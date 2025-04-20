package faang.school.promotionservice.controller;

import faang.school.promotionservice.dto.promotion.RequestPromotionDto;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tariffs")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping()
    public Promotion buyPromotion(@RequestBody RequestPromotionDto requestPromotionDto) {
        return promotionService.buy(requestPromotionDto);
    }

    @PostMapping("/kafka")
    public void testKafka() {
        promotionService.testKafka();
    }
}
