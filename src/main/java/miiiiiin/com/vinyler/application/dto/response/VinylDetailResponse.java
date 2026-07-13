package miiiiiin.com.vinyler.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record VinylDetailResponse(
    DiscogsReleaseDto release, // redis 캐시에서 온 원본
    Long likesCount, // 우리 DB 내용
    Long reviewsCount,
    @JsonProperty("is_liking") boolean isLiking,
    @JsonProperty("is_listened") boolean isListened
) {
}
