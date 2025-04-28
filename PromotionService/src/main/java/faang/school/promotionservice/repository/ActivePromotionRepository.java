package faang.school.promotionservice.repository;

import faang.school.promotionservice.entity.ActivePromotion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivePromotionRepository extends JpaRepository<ActivePromotion, Long> {
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE active_promotions
            SET remaining_impressions = ?1
            WHERE id = ?2
            """)
    @Transactional
    void updateRemainingImpressions(Long remainingImpressions, Long id);
}
