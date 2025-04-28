package faang.school.promotionservice.config.redis;

import faang.school.promotionservice.service.redis.RedisKeyExpirationListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisListenerConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisKeyExpirationListener listener;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listener, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }
}
