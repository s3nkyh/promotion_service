package faang.school.promotionservice.service.redis;

import faang.school.promotionservice.repository.ActivePromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {

    private final ActivePromotionRepository activePromotionRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        if (expiredKey.startsWith(ActivePromotionRedisService.KEY_PREFIX)) {
            Long id = Long.parseLong(expiredKey.replace(ActivePromotionRedisService.KEY_PREFIX, ""));
            activePromotionRepository.deleteById(id);
            log.info("Removed active promotion with id {}", id);
            log.info("Deleted expired ActivePromotion with ID: {}", id);
        }
    }
}
