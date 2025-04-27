package miiiiiin.com.vinyler.application.entity.vinyl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.user.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자가 좋아요를 누르면 DB에 해당 Vinyl을 저장
 */

@Table(name = "vinyls", indexes = @Index(name = "vinyl_userid_idx", columnList = "userid"))
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Vinyl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // DB 내부 관리하는 ID
    private Long vinylId;

    @Column(unique = true, nullable = false)
    // Discogs에서 받아온 Release ID (클라이언트에서 넘어온 값)
    private Long discogsId;

    @Column(nullable = true)
    private Long likesCount = 0L;

    @Column(nullable = true)
    private String title;

    @Column(name = "artists_sort")
    private String artistsSort;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "released_formatted")
    private String releasedFormatted;

    @Column
    private String uri;

    @Column
    private String status;

    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackList> tracklist = new ArrayList<>();

    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Format> formats = new ArrayList<>();

    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArtistDetail> artists = new ArrayList<>();

    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Video> videos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Like 테이블에서 Vinyl을 N:1 관계로 참조
     */
    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore  // 직렬화 시 likes를 무시
    private List<Like> likes = new ArrayList<>();

    public static Vinyl of (LikeRequestDto requestDto, User user) {
        var vinyl = new Vinyl();
        vinyl.setDiscogsId(requestDto.getAlbumInfo().discogsId());
        vinyl.setTitle(requestDto.getAlbumInfo().title());
        vinyl.setLikesCount(vinyl.likesCount);
        vinyl.setArtistsSort(requestDto.getAlbumInfo().artistsSort());
        vinyl.setNotes(requestDto.getAlbumInfo().notes());
        vinyl.setReleasedFormatted(requestDto.getAlbumInfo().releasedFormatted());
        vinyl.setUri(requestDto.getAlbumInfo().uri());
        vinyl.setStatus(requestDto.getAlbumInfo().status());

        // 리스트 데이터 변환 및 연관 관계 설정
        vinyl.getImages().addAll(convertDtoToImages(requestDto, vinyl));
        vinyl.getTracklist().addAll(convertDtoToTracklist(requestDto, vinyl));
        vinyl.getFormats().addAll(convertDtoFormats(requestDto, vinyl));
        vinyl.getVideos().addAll(convertDtoVideos(requestDto, vinyl));
        vinyl.getArtists().addAll(convertDtoArtists(requestDto, vinyl));
        // Vinyl 엔티티의 현재 로그인된 유저정보로 user 세팅
        vinyl.setUser(user);
        return vinyl;
    }
    // Vinyl을 매개변수로 전달하여 연관 관계 설정
    private static List<Image> convertDtoToImages(LikeRequestDto requestDto, Vinyl vinyl) {
        return requestDto.getAlbumInfo().images().stream()
                .map(img -> {
                    Image image = new Image();
                    image.setType(img.getType());
                    image.setUri(img.getUri());
                    // vinyl과 연관 관계 설정
                    image.setVinyl(vinyl);
                    return image;
                })
                .toList();
    }

    private static List<TrackList> convertDtoToTracklist(LikeRequestDto requestDto, Vinyl vinyl) {
        return requestDto.getAlbumInfo().tracklist().stream()
                .map(trackList -> {
                    TrackList list = new TrackList();
                    list.setTitle(trackList.getTitle());
                    list.setDuration(trackList.getDuration());
                    list.setPosition(trackList.getPosition());
                    // vinyl과 연관 관계 설정
                    list.setVinyl(vinyl);
                    return list;
                })
                .toList();
    }

    private static List<Format> convertDtoFormats(LikeRequestDto requestDto, Vinyl vinyl) {
        return requestDto.getAlbumInfo().formats().stream()
                .map(formats -> {
                    Format format = new Format();
                    format.setName(formats.getName());
                    format.setDescriptions(formats.getDescriptions());
                    // vinyl과 연관 관계 설정
                    format.setVinyl(vinyl);
                    return format;
                })
                .toList();
    }

    private static List<Video> convertDtoVideos(LikeRequestDto requestDto, Vinyl vinyl) {
        return requestDto.getAlbumInfo().videos().stream()
                .map(vid -> {
                    Video video = new Video();
                    video.setUri(vid.getUri());
                    // vinyl과 연관 관계 설정
                    video.setVinyl(vinyl);
                    return video;
                })
                .toList();
    }

    private static List<ArtistDetail> convertDtoArtists(LikeRequestDto requestDto, Vinyl vinyl) {
        return requestDto.getAlbumInfo().artists().stream()
                .map(artists -> {
                    ArtistDetail artist = new ArtistDetail();
                    artist.setName(artists.getName());
                    artist.setResourceUrl(artists.getResourceUrl());
                    // vinyl과 연관 관계 설정
                    artist.setVinyl(vinyl);
                    return artist;
                })
                .toList();
    }
}
