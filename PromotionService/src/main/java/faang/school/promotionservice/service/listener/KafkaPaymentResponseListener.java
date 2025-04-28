package faang.school.promotionservice.service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.promotionservice.dto.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPaymentResponseListener {
    public static final String FAILED_PARSE_OBJECT = "Failed to parse object";
    private final ObjectMapper objectMapper;
    private final Object lock = new Object();
    private final Map<String, CompletableFuture<PaymentResponse>> pendingRequests = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${spring.kafka.topics.promotion-payment-response}",
            groupId = "${spring.kafka.group-id.payment-promotion-group-id}")
    public void listen(String message) {
        try {
            PaymentResponse response = objectMapper.readValue(message, PaymentResponse.class);

            if (response.requestId() == null) {
                log.error("Received message with null requestId: {}", message);
                return;
            }

            CompletableFuture<PaymentResponse> future = pendingRequests.remove(response.requestId());
            if (future != null) {
                future.complete(response);
            } else {
                log.warn("No pending request found for ID: {}", response.requestId());
            }
            log.info("we got message");
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message: {}. Error: {}", message, e.getMessage());
            throw new RuntimeException(FAILED_PARSE_OBJECT, e);
        }
    }

    public CompletableFuture<PaymentResponse> registerPendingRequest(String requestId) {
        synchronized (lock) {
            CompletableFuture<PaymentResponse> future = new CompletableFuture<>();
            pendingRequests.put(requestId, future);
            return future;
        }
    }
}
