package faang.school.promotionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.promotionservice.client.UserServiceClient;
import faang.school.promotionservice.dto.Currency;
import faang.school.promotionservice.dto.payment.PaymentRequest;
import faang.school.promotionservice.dto.payment.PaymentResponse;
import faang.school.promotionservice.dto.promotion.PaymentStatus;
import faang.school.promotionservice.dto.promotion.RequestPromotionDto;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.exception.PaymentFailedException;
import faang.school.promotionservice.exception.TariffPriceMismatchException;
import faang.school.promotionservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyMessageFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {
    public static final String TARIFF_PRICE_MISMATCH = "Payment amount does not match the tariff price";
    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    private final ObjectMapper objectMapper;
    private final TariffRepository tariffRepository;
    private final UserServiceClient userServiceClient;

    @Value("${spring.kafka.topics.promotion-payment-request}")
    private String promotionPaymentRequestTopic;

    public Promotion buy(RequestPromotionDto requestPromotionDto) {
        Promotion promotion = tariffRepository.findById(requestPromotionDto.getPromotionId()).get();
        userServiceClient.isExists(requestPromotionDto.getUserId());
        validatePrice(promotion.getUsdPrice(), requestPromotionDto.getAmount());

        PaymentRequest paymentRequest = paymentRequestBuilder(requestPromotionDto.getAmount());
        try {
            String requestJson = objectMapper.writeValueAsString(paymentRequest);
            String correlationId = UUID.randomUUID().toString();

            Message<String> requestMessage = MessageBuilder
                    .withPayload(requestJson)
                    .setHeader(KafkaHeaders.TOPIC, promotionPaymentRequestTopic)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.getBytes(StandardCharsets.UTF_8))
                    .setHeader(KafkaHeaders.REPLY_TOPIC, promotionPaymentRequestTopic)
                    .build();

            RequestReplyMessageFuture<String, String> future =
                    replyingKafkaTemplate.sendAndReceive(requestMessage);

            log.info("Sent message with correlationId: {}", correlationId);

            @SuppressWarnings("unchecked")
            Message<String> replyMessage = (Message<String>) future.get(10, TimeUnit.SECONDS);
            String responseJson = replyMessage.getPayload();
            PaymentResponse paymentResponse = objectMapper.readValue(responseJson, PaymentResponse.class);

            if (!paymentResponse.status().equals(PaymentStatus.SUCCESS)) {
                String errorMsg = String.format("Payment failed for promotion %s. Status: %s, Reason: %s",
                        requestPromotionDto.getPromotionId(),
                        paymentResponse.status()
                );

                log.error("Payment processing failed: {}", errorMsg);
                throw new PaymentFailedException(errorMsg);
            }
            log.info("Received response: {}", paymentResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return promotion;
    }

    public void testKafka() {
        BigDecimal decimal = new BigDecimal("21.01");
        PaymentRequest paymentRequest = paymentRequestBuilder(decimal);
        try {
            String requestJson = objectMapper.writeValueAsString(paymentRequest);
            String correlationId = UUID.randomUUID().toString();

            Message<String> requestMessage = MessageBuilder
                    .withPayload(requestJson)
                    .setHeader(KafkaHeaders.TOPIC, promotionPaymentRequestTopic)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.getBytes(StandardCharsets.UTF_8))
                    .setHeader(KafkaHeaders.REPLY_TOPIC, promotionPaymentRequestTopic)
                    .build();

            RequestReplyMessageFuture<String, String> future =
                    replyingKafkaTemplate.sendAndReceive(requestMessage);

            log.info("Sent message with correlationId: {}", correlationId);

            Message<String> replyMessage = (Message<String>) future.get(10, TimeUnit.SECONDS);
            String responseJson = replyMessage.getPayload();
            PaymentResponse paymentResponse = objectMapper.readValue(responseJson, PaymentResponse.class);

            log.info("Received response: {}", paymentResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void validatePrice(BigDecimal promotionPrice, BigDecimal payerAmount) {
        if (!promotionPrice.equals(payerAmount)) {
            log.error(TARIFF_PRICE_MISMATCH);
            throw new TariffPriceMismatchException(TARIFF_PRICE_MISMATCH);
        }
    }

    private PaymentRequest paymentRequestBuilder(BigDecimal amount) {
        return PaymentRequest.builder()
                .paymentNumber(1)
                .amount(amount)
                .currency(Currency.USD)
                .build();
    }
}