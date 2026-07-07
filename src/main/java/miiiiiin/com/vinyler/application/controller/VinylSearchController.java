package miiiiiin.com.vinyler.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.enums.VinylSortType;
import miiiiiin.com.vinyler.application.dto.response.PopularKeywordDto;
import miiiiiin.com.vinyler.application.dto.response.VinylSearchResultDto;
import miiiiiin.com.vinyler.application.service.PopularKeywordService;
import miiiiiin.com.vinyler.application.service.VinylElasticSearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vinyls")
@RequiredArgsConstructor
@Tag(name = "Vinyl Search", description = "Vinyl 검색(엘라스틱 서치) 엔드포인트")
public class VinylSearchController {

    private final VinylElasticSearchService vinylSearchService;
    private final PopularKeywordService popularKeywordService;

    @GetMapping("/search")
    @Operation(summary = "Vinyl 검색", description = "제목/아티스트를 키워드로 검색합니다. sort로 '리뷰 많은 순(REVIEWS)' 또는 '찜 많은 순(LIKES)'을 선택할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    public ResponseEntity<Page<VinylSearchResultDto>> search(
            @Parameter(description = "검색어 (제목/아티스트)", example = "재즈")
            @RequestParam String keyword,

            @Parameter(description = "정렬 기준", example = "REVIEWS")
            @RequestParam(defaultValue = "REVIEWS")VinylSortType sort,

            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Page<VinylSearchResultDto> result = vinylSearchService.search(keyword, sort, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/search/popular-keywords")
    @Operation(summary = "인기 검색어", description = "가장 많이 검색된 키워드를 상위 N개 반환")
    public ResponseEntity<List<PopularKeywordDto>> popularKeywords(
            @Parameter(description = "가져올 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit,

            @Parameter(description = "최근 몇 시간을 합산할지", example = "2")
            @RequestParam(defaultValue = "2") int hours
    ) {
        return ResponseEntity.ok(popularKeywordService.getTopKeywords(limit, hours));
    }
}

