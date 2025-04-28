package faang.school.promotionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "active_promotions")
public class ActivePromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @ManyToOne
    @JoinColumn(name = "promotion", referencedColumnName = "id", nullable = false)
    private Promotion promotion;

    @Column(name = "remaining_impressions", nullable = false)
    private Long remainingImpressions;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
}
