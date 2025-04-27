package faang.school.promotionservice.service;

import faang.school.promotionservice.client.UserServiceClient;
import faang.school.promotionservice.dto.promotion.ActivePromotionDto;
import faang.school.promotionservice.dto.user.UserDto;
import faang.school.promotionservice.entity.ActivePromotion;
import faang.school.promotionservice.entity.User;
import faang.school.promotionservice.exception.UserNotFoundException;
import faang.school.promotionservice.mapper.ActivePromotionMapper;
import faang.school.promotionservice.mapper.UserMapper;
import faang.school.promotionservice.repository.UserRepository;
import faang.school.promotionservice.service.redis.ActivePromotionRedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final ActivePromotionRedisService activePromotionRedisService;
    private final ActivePromotionMapper activePromotionMapper;
    private final UserServiceClient userServiceClient;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Scheduled(cron = "${spring.scheduler.cron.redis-sync}")
    public void saveAll() {
        List<UserDto> users = userServiceClient.getAllUsers();
        List<User> readyUsers = new ArrayList<>();

        for (UserDto userDto : users) {
            User user = userMapper.toUser(userDto);
            user.setId(userDto.id());
            Optional<ActivePromotionDto> activePromotionDto = activePromotionRedisService.findOneByUserId(userDto.id());

            if (activePromotionDto.isPresent()) {
                ActivePromotionDto activePromotion = activePromotionDto.get();
                user.setPriority(activePromotion.getPriority());
            }
            readyUsers.add(user);
        }

        userRepository.saveAll(readyUsers);
        log.info("Saved {} users", users.size());
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public List<UserDto> searchUsers(String query) {
        try {
            List<User> users = userRepository.universalSearch(query);
            if (users.isEmpty()) {
                log.info("No users found for query: {}", query);
            }
            log.info("Found {} users", users.size());
            List<User> sortedUsers = users.stream()
                    .sorted(Comparator.comparing(User::getPriority).reversed())
                    .toList();
            for (User user : sortedUsers) {
                Optional<ActivePromotionDto> activePromotion = activePromotionRedisService.findById(user.getId());
                if (activePromotion.isPresent()) {
                    ActivePromotionDto activePromotionDto = activePromotion.get();
                    activePromotionDto.setRemainingImpressions(activePromotion.get().getRemainingImpressions() - 1);
                    activePromotionRedisService.save(activePromotionMapper.toActivePromotion(activePromotionDto));
                }
            }
            return userMapper.toUserDtos(sortedUsers);
        } catch (Exception e) {
            log.info("Error searching users", e);
            throw new RuntimeException(e);
        }
    }

    public void setPriority(Long userId, int priority) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found in elastic"));
        user.setPriority(priority);
        userRepository.save(user);
        log.info("Updated user: {}", user);
    }
}
