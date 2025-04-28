package faang.school.promotionservice.mapper;

import faang.school.promotionservice.dto.promotion.ActivePromotionDto;
import faang.school.promotionservice.entity.ActivePromotion;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PromotionMapper.class)
public interface ActivePromotionMapper {
    ActivePromotionDto toActivePromotionDto(ActivePromotion activePromotion);

    ActivePromotion toActivePromotion(ActivePromotionDto activePromotionDto);
}
