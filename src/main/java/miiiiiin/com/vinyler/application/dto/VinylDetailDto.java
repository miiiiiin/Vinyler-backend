package miiiiiin.com.vinyler.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(description = "Vinyl 상세 메타 정보 (DB 데이터 - 좋아요/리뷰 수 및 현재 사용자 상태). 트랙리스트/이미지 등 Discogs 원본 데이터는 클라이언트가 Discogs API를 직접 호출해 조합한다.")
public class VinylDetailDto {
    @Schema(description = "내부 Vinyl ID", example = "1")
    private Long vinylId;

    @Schema(description = "Discogs Release ID", example = "1234567")
    private Long discogsId;

    @Schema(description = "찜 수", example = "128")
    private Long likesCount;

    @Schema(description = "리뷰 수", example = "12")
    private Long reviewsCount;

    @JsonProperty("is_liking")
    @Schema(description = "현재 사용자가 찜한 상태인지 여부", example = "true")
    private boolean isLiking;

    @JsonProperty("is_listened")
    @Schema(description = "현재 사용자가 감상 처리한 상태인지 여부", example = "false")
    private boolean isListened;
}
