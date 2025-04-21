package faang.school.promotionservice.dto.promotion;

import lombok.Builder;

@Builder
public class UnsuccessfulPromotionEvent {
    private Long userId;
    private String message;
}
