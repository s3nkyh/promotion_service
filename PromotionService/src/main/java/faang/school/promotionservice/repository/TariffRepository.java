package faang.school.promotionservice.repository;


import faang.school.promotionservice.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffRepository extends JpaRepository<Promotion, Long> {
}
