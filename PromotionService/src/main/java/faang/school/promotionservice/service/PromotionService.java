package faang.school.promotionservice.service;

import faang.school.promotionservice.client.UserServiceClient;
import faang.school.promotionservice.dto.Currency;
import faang.school.promotionservice.dto.payment.PaymentRequest;
import faang.school.promotionservice.dto.payment.PaymentResponse;
import faang.school.promotionservice.dto.promotion.*;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.exception.TariffPriceMismatchException;
import faang.school.promotionservice.exception.UserNotFoundException;
import faang.school.promotionservice.mapper.PromotionMapper;
import faang.school.promotionservice.repository.PromotionRepository;
import faang.school.promotionservice.service.listener.KafkaPaymentResponseListener;
import faang.school.promotionservice.service.publisher.KafkaEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {
    public static final String TARIFF_PRICE_MISMATCH = "Payment amount does not match the tariff price";
    public static final String USER_NOT_FOUND = "It looks like you haven't registered." +
            " Please register and continue paying again.";
    private final KafkaEventPublisher kafkaEventPublisher;
    private final KafkaPaymentResponseListener kafkaPaymentResponseListener;
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final UserServiceClient userServiceClient;

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
                createSuccessPromotionEvent(requestPromotionDto.getUserId(), promotion.getName())); // доавбить Listener в NotificationService


        return promotionMapper.toPromotionDto(promotion);
    }

    private void processPaymentAndVerify(CompletableFuture<PaymentResponse> responseFuture,
                                         PaymentRequest paymentRequest, Long userId) {
        try {
            PaymentResponse response = responseFuture.get(10, TimeUnit.SECONDS);
            if (!response.status().equals(PaymentStatus.SUCCESS)) {
                kafkaEventPublisher.publishEvent(promotionNotificationTopic, createUnsuccessfulPromotionEvent(userId)); // доавбить Listener в NotificationService
                throw new RuntimeException("Payment failed");
            }
            log.info("Payment successful");
        } catch (TimeoutException e) {
            log.error("Payment response timeout for requestId: {}", paymentRequest.requestId());
            kafkaEventPublisher.publishEvent(promotionNotificationTopic, createUnsuccessfulPromotionEvent(userId));
            throw new RuntimeException("Payment timeout");
        } catch (InterruptedException | ExecutionException e) {
            log.error("Payment processing failed", e);
            kafkaEventPublisher.publishEvent(promotionNotificationTopic, createUnsuccessfulPromotionEvent(userId));
            throw new RuntimeException("Payment error");
        }
    }

    private void validatePrice(BigDecimal promotionPrice, BigDecimal payerAmount) {
        if (!promotionPrice.equals(payerAmount)) {
            log.error(TARIFF_PRICE_MISMATCH);
            throw new TariffPriceMismatchException(TARIFF_PRICE_MISMATCH);
        }
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

    private SuccessPromotionEvent createSuccessPromotionEvent(Long userId, String promotionName) {
        String message = String.format("Congratulations, you have bought a %s promotion", promotionName);

        return SuccessPromotionEvent.builder()
                .userId(userId)
                .message(message)
                .build();
    }

    private UnsuccessfulPromotionEvent createUnsuccessfulPromotionEvent(Long userId) {
        String message = "The payment failed. Repeat the payment operation";

        return UnsuccessfulPromotionEvent.builder()
                .userId(userId)
                .message(message)
                .build();
    }
}