package faang.school.promotionservice.dto.promotion;

import faang.school.promotionservice.dto.TargetTypeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDto {
    private Long id;
    private String name;
}
