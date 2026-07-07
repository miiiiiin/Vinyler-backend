package miiiiiin.com.vinyler.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인기 검색어")
public record PopularKeywordDto(
        @Schema(description = "순위(1부터)", example = "1") long rank,
        @Schema(description = "검색어", example = "재즈") String keyword,
        @Schema(description = "검색 횟수", example = "153") long count
) {
}
