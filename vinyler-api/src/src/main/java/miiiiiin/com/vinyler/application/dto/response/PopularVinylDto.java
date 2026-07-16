package miiiiiin.com.vinyler.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PopularVinylDto(
        @Schema(description = "인기 음반 순위")
        long rank,
        @Schema(description = "음반 id")
        Long discogsId,
        @Schema(description = "점수")
        long score,

        @Schema(description = "제목")
        String title,

        @Schema(description = "아티스트")
        String artistsSort
) {
}
