package faang.school.promotionservice.mapper;

import faang.school.promotionservice.dto.promotion.PromotionDto;
import faang.school.promotionservice.dto.TargetTypeDto;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.entity.TargetType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TariffMapper {

    PromotionDto toTariffDto(Promotion promotion);

    Promotion toTariff(PromotionDto promotionDto);

    default TargetTypeDto targetTypeToDto(TargetType targetType) {
        if (targetType == null) {
            return null;
        }

        return new TargetTypeDto(targetType.name());
    }

    default TargetType targetTypeToEntity(TargetTypeDto targetTypeDto) {
        return TargetType.valueOf(targetTypeDto.getName());
    }
}
