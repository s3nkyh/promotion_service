package faang.school.promotionservice.service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.promotionservice.dto.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class KafkaPaymentResponseListener {
    public static final String FAILED_PARSE_OBJECT = "Failed to parse object";
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topics.promotion-payment-response}",
    groupId = "${spring.kafka.group-id.payment-promotion-group-id}")
    public void listen(String message) {
        try {
            PaymentResponse paymentResponse = objectMapper.readValue(message, PaymentResponse.class);
            log.info("Message was handled {}", paymentResponse.toString());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message: {}. Error: {}", message, e.getMessage());
            throw new RuntimeException(FAILED_PARSE_OBJECT);
        }
    }
}
