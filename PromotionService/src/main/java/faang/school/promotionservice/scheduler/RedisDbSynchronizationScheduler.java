package faang.school.promotionservice.scheduler;

import faang.school.promotionservice.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisDbSynchronizationScheduler {
    private final PromotionService promotionService;

    @Scheduled(cron = "${spring.scheduler.cron.redis-sync}")
    public void scheduleRedisDbSynchronization() {
        log.info("Starting synchronization data from Redis to BD");
        long startTime = System.currentTimeMillis();

        try {
           promotionService.syncFromRedisToDb();
           log.info("Synchronization completed in {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Synchronization failed", e);
        }
    }
}
