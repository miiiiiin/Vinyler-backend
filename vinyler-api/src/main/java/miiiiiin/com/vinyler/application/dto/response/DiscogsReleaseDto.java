package miiiiiin.com.vinyler.application.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// DISCOGS가 주는 나머지 필드는 조용히 버림
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscogsReleaseDto(
        Long id,
        String title,
        @JsonProperty("artists_sort") String artistsSort,
        @JsonProperty("released_formatted") String releasedFormatted,
        String notes,
        List<String> genres,
        List<Tracklist> tracklist,
        List<Image> images

) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tracklist(String position, String title, String duration) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String uri, String type) {}

}
