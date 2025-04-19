package faang.school.promotionservice.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "impressions", nullable = false)
    private Long impressions;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "usd_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal usdPrice;

    @Min(1) @Max(10)
    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "is_unlimited", columnDefinition = "boolean default false")
    private boolean isUnlimited;
}
