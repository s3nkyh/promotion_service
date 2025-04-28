package faang.school.promotionservice.controller;

import faang.school.promotionservice.dto.user.UserDto;
import faang.school.promotionservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/searches")
public class SearchController {
    private final SearchService searchService;

    @PostMapping
    public void saveAll() {
        searchService.saveAll();
    }

    @DeleteMapping
    public void deleteAll() {
        searchService.deleteAll();
    }

    @GetMapping("/{query}")
    public List<UserDto> search(@PathVariable String query) {
        return searchService.searchUsers(query);
    }
}
