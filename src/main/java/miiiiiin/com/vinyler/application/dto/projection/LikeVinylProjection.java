package miiiiiin.com.vinyler.application.dto.projection;

public interface LikeVinylProjection {
    Long getVinylId();
    Long getDiscogsId();
    String getTitle();
    String getArtistsSort();
    Long getLikesCount();
    String getNotes();
    String getStatus();
    String getUri();
    String getReleasedFormatted();
}
