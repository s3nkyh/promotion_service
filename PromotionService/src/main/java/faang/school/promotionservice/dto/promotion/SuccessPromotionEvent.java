package faang.school.promotionservice.dto.promotion;

import lombok.Builder;

@Builder
public class SuccessPromotionEvent {
    private Long userId;
    private String message;
}
