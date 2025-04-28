package faang.school.promotionservice.dto.user;

import faang.school.promotionservice.dto.SkillDto;

public record UserDto(
        Long id,
        String username,
        String aboutMe,
        SkillDto skillDto,
        int priority
) {
}
