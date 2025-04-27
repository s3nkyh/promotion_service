package faang.school.promotionservice.mapper;

import faang.school.promotionservice.dto.SkillDto;
import faang.school.promotionservice.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillDto toDto(Skill skill);

    Skill toEntity(SkillDto skillDto);

    List<SkillDto> toDtoList(List<Skill> skills);
}
