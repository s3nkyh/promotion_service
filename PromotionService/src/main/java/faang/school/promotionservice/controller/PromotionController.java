package faang.school.promotionservice.controller;

import faang.school.promotionservice.dto.promotion.ActivePromotionDto;
import faang.school.promotionservice.dto.promotion.PromotionDto;
import faang.school.promotionservice.dto.promotion.RequestPromotionDto;
import faang.school.promotionservice.entity.ActivePromotion;
import faang.school.promotionservice.entity.Promotion;
import faang.school.promotionservice.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping()
    public PromotionDto buyPromotion(@RequestBody RequestPromotionDto requestPromotionDto) {
        return promotionService.buy(requestPromotionDto);
    }

    @GetMapping("/{activePromotionId}")
    public ActivePromotionDto getActivePromotion(@PathVariable Long activePromotionId) {
        return promotionService.getActivePromotion(activePromotionId);
    }
}
