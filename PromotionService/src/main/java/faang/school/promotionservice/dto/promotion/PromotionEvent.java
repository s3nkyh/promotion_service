package faang.school.promotionservice.dto.promotion;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PromotionEvent {
    private Long userId;
    private String message;
}
