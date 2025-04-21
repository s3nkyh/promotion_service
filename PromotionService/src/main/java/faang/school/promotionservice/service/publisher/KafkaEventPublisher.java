package faang.school.promotionservice.service.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher {
    public static final String FAILED_SERIALIZING_OBJECT = "Failed to serialize object";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

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
}
