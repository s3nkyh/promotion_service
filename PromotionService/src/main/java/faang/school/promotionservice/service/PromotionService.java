package faang.school.promotionservice.service;

import faang.school.promotionservice.client.UserServiceClient;
import faang.school.promotionservice.dto.Currency;
import faang.school.promotionservice.dto.payment.PaymentRequest;
import faang.school.promotionservice.dto.payment.PaymentResponse;
import faang.school.promotionservice.dto.payment.PaymentStatus;
import faang.school.promotionservice.dto.promotion.*;
import faang.school.promotionservice.entity.ActivePromotion;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.exception.PaymentFailedException;
import faang.school.promotionservice.exception.TariffPriceMismatchException;
import faang.school.promotionservice.exception.UserNotFoundException;
import faang.school.promotionservice.mapper.ActivePromotionMapper;
import faang.school.promotionservice.mapper.PromotionMapper;
import faang.school.promotionservice.repository.ActivePromotionRepository;
import faang.school.promotionservice.repository.PromotionRepository;
import faang.school.promotionservice.service.listener.KafkaPaymentResponseListener;
import faang.school.promotionservice.service.publisher.KafkaEventPublisher;
import faang.school.promotionservice.service.redis.ActivePromotionRedisService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static faang.school.promotionservice.message.ErrorMessage.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {
    private final static String SUCCESS_MESSAGE = "Congratulations, you have bought a promotion";
    private final static String UNSUCCESSFUL_MESSAGE = "The payment failed. Repeat the payment operation";
    private final KafkaEventPublisher kafkaEventPublisher;
    private final KafkaPaymentResponseListener kafkaPaymentResponseListener;
    private final PromotionRepository promotionRepository;
    private final ActivePromotionRepository activePromotionRepository;
    private final ActivePromotionRedisService activePromotionRedisService;
    private final PromotionMapper promotionMapper;
    private final ActivePromotionMapper activePromotionMapper;
    private final SearchService searchService;
    private final UserServiceClient userServiceClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Value("${spring.kafka.topics.promotion-payment-request}")
    private String promotionPaymentRequestTopic;

    @Value("${spring.kafka.topics.promotion-notification-topic}")
    private String promotionNotificationTopic;

    @Transactional
    public PromotionDto buy(RequestPromotionDto requestPromotionDto) {
        Promotion promotion = promotionRepository.findById(requestPromotionDto.getPromotionId()).get();
        if (!userServiceClient.isExists(requestPromotionDto.getUserId())) {
            log.info("User not found");
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        validatePrice(promotion.getUsdPrice(), requestPromotionDto.getAmount());

        PaymentRequest paymentRequest = paymentRequestBuilder(requestPromotionDto.getAmount());

        CompletableFuture<PaymentResponse> responseFuture =
                kafkaPaymentResponseListener.registerPendingRequest(paymentRequest.requestId());
        kafkaEventPublisher.publishEvent(promotionPaymentRequestTopic, paymentRequest);

        processPaymentAndVerify(responseFuture, paymentRequest, requestPromotionDto.getUserId());
        kafkaEventPublisher.publishEvent(promotionNotificationTopic,
                createPromotionEvent(requestPromotionDto.getUserId(), promotion.getName())); // доавбить Listener в NotificationService
        log.info("Notification was has been sent");

        ActivePromotion activePromotion = createActivePromotion(requestPromotionDto.getUserId(), promotion);
        activePromotionRepository.save(activePromotion);
        log.info("Active promotion created {}", activePromotion);

        activePromotionRedisService.save(activePromotion);
        searchService.setPriority(activePromotion.getUserId(), activePromotion.getPriority());

        return promotionMapper.toPromotionDto(promotion);
    }

    public ActivePromotionDto getActivePromotion(Long activePromotionId) {
        Optional<ActivePromotionDto> cachedPromotion = activePromotionRedisService.findById(activePromotionId);

        if (cachedPromotion.isPresent()) {
            log.info("Active promotion found {}", cachedPromotion.get());
            return cachedPromotion.get();
        }

        ActivePromotion activePromotion = activePromotionRepository.findById(activePromotionId)
                .orElseThrow(() -> new EntityNotFoundException(ACTIVE_PROMOTION_NOT_FOUND));

        activePromotionRedisService.save(activePromotion);

        return activePromotionMapper.toActivePromotionDto(activePromotion);
    }

    public void syncFromRedisToDb() {
        List<ActivePromotionDto> redisPromotions = activePromotionRedisService.findAll();
        log.info("Found {} promotions", redisPromotions.size());

        List<CompletableFuture<Void>> updateFutures = redisPromotions.stream()
                .map(dto -> CompletableFuture.runAsync(
                        () -> activePromotionRepository.updateRemainingImpressions(
                                dto.getRemainingImpressions(),
                                dto.getId()
                        ),
                        executorService
                ))
                .toList();

        List<CompletableFuture<Void>> cleanupFutures = redisPromotions.stream()
                .filter(dto -> !dto.isActive())
                .map(dto -> CompletableFuture.runAsync(
                        () -> {
                            activePromotionRedisService.deleteById(dto.getId());
                            activePromotionRepository.deleteById(dto.getId());
                        },
                        executorService
                )).toList();

        CompletableFuture.allOf(
                Stream.concat(updateFutures.stream(), cleanupFutures.stream())
                        .toArray(CompletableFuture[]::new)
        ).join();
    }

    private void processPaymentAndVerify(CompletableFuture<PaymentResponse> responseFuture,
                                         PaymentRequest paymentRequest, Long userId) {
        try {
            PaymentResponse response = responseFuture.get(20, TimeUnit.SECONDS);

            if (!response.status().equals(PaymentStatus.SUCCESS)) {
                kafkaEventPublisher.publishEvent(promotionNotificationTopic, createPromotionEvent(userId, UNSUCCESSFUL_MESSAGE)); // доавбить Listener в NotificationService
                log.error(PAYMENT_FAILED);
                throw new PaymentFailedException(PAYMENT_FAILED);
            }

            kafkaEventPublisher.publishEvent(promotionNotificationTopic, createPromotionEvent(userId, SUCCESS_MESSAGE));
            log.info("Payment successful");
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            log.error("Payment processing failed for requestId: {}", paymentRequest.requestId());
            kafkaEventPublisher.publishEvent(promotionNotificationTopic, createPromotionEvent(userId, UNSUCCESSFUL_MESSAGE));
            throw new PaymentFailedException(PAYMENT_FAILED);
        }
    }

    private void validatePrice(BigDecimal promotionPrice, BigDecimal payerAmount) {
        if (!promotionPrice.equals(payerAmount)) {
            log.error(TARIFF_PRICE_MISMATCH);
            throw new TariffPriceMismatchException(TARIFF_PRICE_MISMATCH);
        }
    }

    private ActivePromotion createActivePromotion(Long userId, Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();

        return ActivePromotion.builder()
                .userId(userId)
                .targetType(promotion.getTargetType())
                .targetId(null)
                .promotion(promotion)
                .remainingImpressions(promotion.getImpressions())
                .priority(promotion.getPriority())
                .startTime(now)
                .endTime(now.plusDays(promotion.getDurationDays()))
                .build();
    }

    private PaymentRequest paymentRequestBuilder(BigDecimal amount) {
        String requestId = UUID.randomUUID().toString();

        return PaymentRequest.builder()
                .requestId(requestId)
                .paymentNumber(1)
                .amount(amount)
                .currency(Currency.USD)
                .build();
    }

    private PromotionEvent createPromotionEvent(Long userId, String message) {
        return PromotionEvent.builder()
                .userId(userId)
                .message(message)
                .build();
    }
}