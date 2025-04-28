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
public class UpdateActivePromotionDto {
    private Long id;
    private Long remainingImpressions;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int priority;
}
