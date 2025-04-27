package faang.school.promotionservice.scheduler;

import faang.school.promotionservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerUsersToElastic {
    private final SearchService searchService;

    @Scheduled(cron = "${spring.scheduler.cron.users-to-elastic}")
    public void usersToElastic() {
        log.info("Starting adding operation users to ElasticSearch");
        long startTime = System.currentTimeMillis();

        try {
            searchService.saveAll();
            log.info("The addition operation completed in {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Operation failed", e);
        }
    }
}
