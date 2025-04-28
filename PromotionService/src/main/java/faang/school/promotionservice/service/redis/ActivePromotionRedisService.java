package faang.school.promotionservice.service.redis;

import faang.school.promotionservice.dto.promotion.ActivePromotionDto;
import faang.school.promotionservice.dto.promotion.PromotionDto;
import faang.school.promotionservice.dto.promotion.UpdateActivePromotionDto;
import faang.school.promotionservice.entity.ActivePromotion;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.repository.ActivePromotionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Service
public class ActivePromotionRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ActivePromotionRepository activePromotionRepository;
    public static final String KEY_PREFIX = "activePromotion:";

    public void save(ActivePromotion activePromotion) {
        String key = KEY_PREFIX + activePromotion.getId();
        Promotion promotion = activePromotion.getPromotion();

        Map<String, Object> entries = new HashMap<>();
        entries.put("id", String.valueOf(activePromotion.getId()));
        entries.put("remainingImpressions", String.valueOf(activePromotion.getRemainingImpressions()));
        entries.put("userId", String.valueOf(activePromotion.getUserId()));
        entries.put("endTime", activePromotion.getEndTime().toString());
        entries.put("startTime", activePromotion.getStartTime().toString());
        entries.put("priority", String.valueOf(activePromotion.getPriority()));

        entries.put("promotionId", String.valueOf(promotion.getId()));
        entries.put("promotionName", promotion.getName());
        redisTemplate.opsForHash().putAll(key, entries);

        long ttl = ChronoUnit.SECONDS.between(LocalDateTime.now(), activePromotion.getEndTime());
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    public Optional<ActivePromotionDto> findById(Long id) {
        String key = KEY_PREFIX + id;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertToActivePromotion(entries));
    }

    public Optional<ActivePromotionDto> findOneByUserId(Long userId) {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        if (keys != null) {
            for (String key : keys) {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                if (!entries.isEmpty()) {
                    Long entryUserId = Long.parseLong((String) entries.get("userId"));
                    if (entryUserId.equals(userId)) {
                        ActivePromotionDto dto = convertToActivePromotion(entries);
                        dto.setId(Long.parseLong(key.replace(KEY_PREFIX, "")));
                        return Optional.of(dto);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public List<ActivePromotionDto> findAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        List<ActivePromotionDto> activePromotions = new ArrayList<>();

        if (keys != null) {
            for (String key : keys) {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                if (!entries.isEmpty()) {
                    ActivePromotionDto activePromotion = convertToActivePromotion(entries);
                    activePromotion.setId(Long.parseLong(key.replace(KEY_PREFIX, "")));
                    activePromotions.add(activePromotion);
                }
            }
        }
        return activePromotions;
    }

    public void deleteById(Long id) {
        String key = KEY_PREFIX + id;
        redisTemplate.delete(key);
    }

    private ActivePromotionDto convertToActivePromotion(Map<Object, Object> entries) {
        return ActivePromotionDto.builder()
                .id(Long.parseLong((String) entries.get("id")))
                .userId(Long.parseLong((String) entries.get("userId")))
                .promotion(
                        PromotionDto.builder()
                                .id(Long.parseLong((String) entries.get("promotionId")))
                                .name((String) entries.get("promotionName"))
                                .build()
                )
                .remainingImpressions(Long.parseLong((String) entries.get("remainingImpressions")))
                .startTime(LocalDateTime.parse((String) entries.get("startTime")))
                .endTime(LocalDateTime.parse((String) entries.get("endTime")))
                .priority(Integer.parseInt((String) entries.get("priority")))
                .build();
    }

    @PostConstruct
    public void loadDataIntoRedis() {
        List<ActivePromotion> activePromotions = activePromotionRepository.findAll();
        activePromotions.forEach(this::save);
    }
}
