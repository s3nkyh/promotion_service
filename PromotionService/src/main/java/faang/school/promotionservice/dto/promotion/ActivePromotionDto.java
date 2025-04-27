package faang.school.promotionservice.dto.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import faang.school.promotionservice.dto.TargetTypeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivePromotionDto {
    private Long id;
    private Long userId;
    private TargetTypeDto targetType;
    private PromotionDto promotion;
    private Long remainingImpressions;
    private int priority;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    public boolean isActive() {
        return LocalDateTime.now().isBefore(endTime) && remainingImpressions > 0;
    }
}
