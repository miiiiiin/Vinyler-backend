package miiiiiin.com.vinyler.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vinyl 찜/감상 요청 (Discogs 음반 식별 정보 포함)")
public record LikeRequestDto(
    @JsonProperty("discogs_id")
    @Schema(description = "Discogs Release ID", example = "1234567")
    Long discogsId,

    @Schema(description = "음반 제목", example = "Kind of Blue")
    String title,

    @JsonProperty("artists_sort")
    @Schema(description = "아티스트 이름", example = "Miles Davis")
    String artistsSort,

    @JsonProperty("released_formatted")
    @Schema(description = "출시일 (포맷 문자열)", example = "1959")
    String releasedFormatted) {
}
