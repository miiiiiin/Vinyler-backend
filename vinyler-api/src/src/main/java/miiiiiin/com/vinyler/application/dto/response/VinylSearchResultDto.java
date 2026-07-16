package miiiiiin.com.vinyler.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import miiiiiin.com.vinyler.application.document.VinylDocument;

@Builder
@Schema(description = "Vinyl 검색 결과(엘라스틱 서치)")
public record VinylSearchResultDto(
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
        String releasedFormatted,

        @Schema(description = "찜 수", example = "123")
        Long likesCount,

        @Schema(description = "리뷰 수", example = "12")
        Long reviewsCount
) {

        // 엘라스틱 색인 문서 -> 클라이언트 응답 DTO
        public static VinylSearchResultDto from(VinylDocument doc) {
                return VinylSearchResultDto.builder()
                        .discogsId(doc.getDiscogsId())
                        .title(doc.getTitle())
                        .artistsSort(doc.getArtistsSort())
                        .releasedFormatted(doc.getReleasedFormatted())
                        .likesCount(doc.getLikesCount())
                        .reviewsCount(doc.getReviewsCount())
                        .build();
        }
}
