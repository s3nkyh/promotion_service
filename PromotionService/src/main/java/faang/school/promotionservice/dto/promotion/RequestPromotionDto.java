package faang.school.promotionservice.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPromotionDto {
    private Long userId;
    private BigDecimal amount;
    private Long promotionId;
}
