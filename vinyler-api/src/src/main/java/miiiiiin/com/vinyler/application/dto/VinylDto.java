package miiiiiin.com.vinyler.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import miiiiiin.com.vinyler.application.entity.*;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;

@Builder
@Data
@Schema(description = "Vinyl(LP 음반) 정보")
public class VinylDto {
    @Schema(description = "내부 Vinyl ID", example = "1")
    private Long vinylId;

    @Schema(description = "Discogs Release ID", example = "1234567")
    private Long discogsId;

    @Schema(description = "음반 제목", example = "Kind of Blue")
    private String title;

    @Schema(description = "아티스트 이름", example = "Miles Davis")
    private String artistsSort;

    @Schema(description = "찜 수", example = "128")
    private Long likesCount;

    @Schema(description = "리뷰 수", example = "12")
    private Long reviewsCount;

    @Schema(description = "출시일 (포맷 문자열)", example = "1959")
    private String releasedFormatted;

    public static VinylDto of(Like like) {
        Vinyl vinyl = like.getVinyl();
        return getVinylDto(vinyl);
    }

    public static VinylDto of(UserVinylStatus status) {
        Vinyl vinyl = status.getVinyl();
        return getVinylDto(vinyl);
    }

    public static VinylDto of(Vinyl vinyl) {
        return getVinylDto(vinyl);
    }

    private static VinylDto getVinylDto(Vinyl vinyl) {
        return VinylDto.builder()
            .vinylId(vinyl.getVinylId())
            .discogsId(vinyl.getDiscogsId())
            .title(vinyl.getTitle())
            .artistsSort(vinyl.getArtistsSort())
            .likesCount(vinyl.getLikesCount())
            .reviewsCount(vinyl.getReviewsCount())
            .releasedFormatted(vinyl.getReleasedFormatted())
            .build();
    }
}
