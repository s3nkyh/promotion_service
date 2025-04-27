package faang.school.promotionservice.mapper;

import faang.school.promotionservice.dto.promotion.PromotionDto;
import faang.school.promotionservice.dto.TargetTypeDto;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.entity.TargetType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {

    PromotionDto toPromotionDto(Promotion promotion);

    Promotion toPromotion(PromotionDto promotionDto);

    default TargetTypeDto targetTypeToDto(TargetType targetType) {
        if (targetType == null) {
            return null;
        }

        return new TargetTypeDto(targetType.name());
    }

    default TargetType targetTypeToEntity(TargetTypeDto targetTypeDto) {
        if (targetTypeDto == null) {
            return null;
        }
        return TargetType.valueOf(targetTypeDto.getName());
    }
}
