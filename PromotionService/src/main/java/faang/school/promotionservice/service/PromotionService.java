package faang.school.promotionservice.service;

import faang.school.promotionservice.client.UserServiceClient;
import faang.school.promotionservice.dto.Currency;
import faang.school.promotionservice.dto.payment.PaymentRequest;
import faang.school.promotionservice.dto.promotion.RequestPromotionDto;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.exception.TariffPriceMismatchException;
import faang.school.promotionservice.repository.TariffRepository;
import faang.school.promotionservice.service.publisher.KafkaPaymentRequestPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {
    public static final String TARIFF_PRICE_MISMATCH = "Payment amount does not match the tariff price";
    private final TariffRepository tariffRepository;
    private final UserServiceClient userServiceClient;
    private final KafkaPaymentRequestPublisher kafkaPaymentRequestPublisher;

    @Value("${spring.kafka.topics.promotion-payment-request}")
    private String promotionPaymentTopic;

    public Promotion buy(RequestPromotionDto requestPromotionDto) {
        Promotion promotion = tariffRepository.findById(requestPromotionDto.getPromotionId()).get();
//        userServiceClient.isExists(requestPromotionDto.getUserId());
        validatePrice(promotion.getUsdPrice(), requestPromotionDto.getAmount());
        PaymentRequest paymentRequest = paymentRequestBuilder(requestPromotionDto.getAmount());
        kafkaPaymentRequestPublisher.publishEvent(promotionPaymentTopic, paymentRequest);
        return promotion;
    }

    public void testKafka() {
        BigDecimal decimal = new BigDecimal("21.01");
        kafkaPaymentRequestPublisher.publishEvent(promotionPaymentTopic, paymentRequestBuilder(decimal));
    }

    private void validatePrice(BigDecimal promotionPrice, BigDecimal payerAmount) {
        if (!promotionPrice.equals(payerAmount)) {
            log.error(TARIFF_PRICE_MISMATCH);
            throw new TariffPriceMismatchException(TARIFF_PRICE_MISMATCH);
        }
    }

    private PaymentRequest paymentRequestBuilder (BigDecimal amount) {
        return PaymentRequest.builder()
                .paymentNumber(1)
                .amount(amount)
                .currency(Currency.USD)
                .build();
    }
}