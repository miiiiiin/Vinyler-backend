package miiiiiin.com.vinyler.application.entity;

import jakarta.persistence.*;
import lombok.*;
import miiiiiin.com.vinyler.application.dto.ImageDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.user.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @JoinColumn(name = "userid")
    private User user;

    /**
     * Like 테이블에서 Vinyl을 N:1 관계로 참조
     */
    @OneToMany(mappedBy = "vinyl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vinyl vinyl = (Vinyl) o;
        return Objects.equals(vinylId, vinyl.vinylId) && Objects.equals(likesCount, vinyl.likesCount) && Objects.equals(artistsSort, vinyl.artistsSort) && Objects.equals(notes, vinyl.notes) && Objects.equals(releasedFormatted, vinyl.releasedFormatted) && Objects.equals(uri, vinyl.uri) && Objects.equals(status, vinyl.status) && Objects.equals(images, vinyl.images) && Objects.equals(tracklist, vinyl.tracklist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vinylId, likesCount, artistsSort, notes, releasedFormatted, uri, status, images, tracklist);
    }

    public static Vinyl of (LikeRequestDto requestDto, User user) {
        var vinyl = new Vinyl();
        vinyl.setDiscogsId(requestDto.getDiscogsId());
        vinyl.setLikesCount(vinyl.likesCount);
        vinyl.setArtistsSort(requestDto.getArtistsSort());
        vinyl.setNotes(requestDto.getNotes());
        vinyl.setReleasedFormatted(requestDto.getReleasedFormatted());
        vinyl.setUri(requestDto.getUri());
        vinyl.setStatus(requestDto.getStatus());

        // Images 변환 및 연관 관계 설정
//        List<Image> images = convertDtoToImages(requestDto, vinyl);
//        vinyl.setImages(images);
        vinyl.getImages().addAll(convertDtoToImages(requestDto, vinyl));

//        vinyl.setImages(convertDtoToImages(requestDto));
//        vinyl.setTracklist(requestDto.getTracklist());
//        vinyl.setArtists(requestDto.getArtists());
//        vinyl.setFormats(requestDto.getFormats());
//        vinyl.setVideos(requestDto.getVideos());

        // Vinyl 엔티티의 현재 로그인된 유저정보로 user 세팅
        vinyl.setUser(user);
        return vinyl;
    }
    // Vinyl을 매개변수로 전달하여 연관 관계 설정
    private static List<Image> convertDtoToImages(LikeRequestDto requestDto, Vinyl vinyl) {
        return requestDto.getImages().stream()
                .map(imageDto -> {
                    Image image = new Image();
                    image.setType(imageDto.getType());
                    image.setUri(imageDto.getUri());
                    // vinyl과 연관 관계 설정
                    image.setVinyl(vinyl);
                    return image;
                })
                .toList();
    }

    // 연관관계 편의 메서드
    public void addImage(Image image) {
        images.add(image);
        image.setVinyl(this);  // 연관 관계 설정
    }


}
