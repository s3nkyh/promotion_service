package faang.school.promotionservice.service.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher {
    public static final String FAILED_SERIALIZING_OBJECT = "Failed to serialize object";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Retryable(
            maxAttemptsExpression = "${spring.retry.kafka-publisher.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${spring.retry.kafka-publisher.initial-delay}",
                    multiplierExpression = "${spring.retry.kafka-publisher.multiplier}"
            )
    )
    public void publishEvent(String topic, Object object) {
        try {
            String message = objectMapper.writeValueAsString(object);
            kafkaTemplate.send(topic, message);
        } catch (JsonProcessingException e) {
            log.error("Serialization error for object: {}", object, e);
            throw new RuntimeException(FAILED_SERIALIZING_OBJECT, e);
        }
        log.info("Sent object: {}, to topic: {}", object.getClass().getName(), topic);
    }

    @Recover
    public void recover(RuntimeException e, String topic, Object object) {
        log.error("All retries failed for topic: {}, object: {}", topic, object, e);
    }
}
