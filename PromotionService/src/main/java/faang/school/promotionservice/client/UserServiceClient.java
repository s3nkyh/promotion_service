package faang.school.promotionservice.client;

import faang.school.promotionservice.dto.user.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service", url = "${user-service.host}:${user-service.port}")
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{userId}")
    UserDto getUser(@PathVariable long userId);

    @GetMapping("/api/v1/users/exists/{userId}")
    boolean isExists(@PathVariable long userId);

    @GetMapping("/api/v1/users")
    List<UserDto> getAllUsers();
}
