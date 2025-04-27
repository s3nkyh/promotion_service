package faang.school.promotionservice.mapper;

import faang.school.promotionservice.dto.user.UserDto;
import faang.school.promotionservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = SkillMapper.class)
public interface UserMapper {
    @Mapping(target = "skill", source = "skillDto")
    User toUser(UserDto userDto);

    @Mapping(target = "skillDto", source = "skill")
    UserDto toUser(User user);

    List<User> toUsers(List<UserDto> userDtos);
    List<UserDto> toUserDtos(List<User> users);
}
