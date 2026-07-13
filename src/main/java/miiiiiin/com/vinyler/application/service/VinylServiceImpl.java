package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDetailDto;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.dto.response.DiscogsReleaseDto;
import miiiiiin.com.vinyler.application.dto.response.VinylDetailResponse;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.event.VinylIndexSyncEvent;
import miiiiiin.com.vinyler.application.event.VinylLikedEvent;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.discogs.service.VinylDetailCacheService;
import miiiiiin.com.vinyler.global.Constants;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VinylServiceImpl implements VinylService {

    private final VinylRepository vinylRepository;
    private final LikeRepository likeRepository;
    private final UserVinylStatusRepository userVinylStatusRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PopularVinylService popularVinylService;
    private final VinylDetailCacheService vinylDetailCacheService;

    @Override
    @Transactional
    public Vinyl findOrCreateVinyl(LikeRequestDto requestDto, User user) {
        return vinylRepository.findByDiscogsId(requestDto.discogsId())
                .orElseGet(() -> {
                    Vinyl saved = vinylRepository.save(buildVinylFromDto(requestDto, user));
                    eventPublisher.publishEvent(new VinylIndexSyncEvent(saved.getDiscogsId()));
                    return saved;
                });
    }

    @Override
    @Transactional
    public VinylLikeDto toggleLike(LikeRequestDto requestDto, User currentUser) {
        var vinylEntity = findOrCreateVinyl(requestDto, currentUser);

        // 사용자와 Vinyl에 대한 Like 조회
        var likeEntity = likeRepository.findByUserAndVinyl(currentUser, vinylEntity);

        if (likeEntity.isPresent()) {
            likeRepository.delete(likeEntity.get());
            vinylEntity.setLikesCount(Math.max(0, vinylEntity.getLikesCount() - 1));
            var result = VinylLikeDto.from(vinylRepository.save(vinylEntity), currentUser, false);
            eventPublisher.publishEvent(new VinylIndexSyncEvent(vinylEntity.getDiscogsId()));
            // 취소 : -1
            eventPublisher.publishEvent(new VinylLikedEvent(UUID.randomUUID().toString(), vinylEntity.getDiscogsId(), -1));
            return result;
        } else {
            likeRepository.save(Like.of(currentUser, vinylEntity));
            vinylEntity.setLikesCount(vinylEntity.getLikesCount() + 1);
            var result = VinylLikeDto.from(vinylRepository.save(vinylEntity), currentUser, true);
            eventPublisher.publishEvent(new VinylIndexSyncEvent(vinylEntity.getDiscogsId()));
            // 좋아요 : +1
            eventPublisher.publishEvent(new VinylLikedEvent(UUID.randomUUID().toString(), vinylEntity.getDiscogsId(), +1));
            return result;
        }
    }

    private Vinyl buildVinylFromDto(LikeRequestDto requestDto, User user) {
        var vinyl = new Vinyl();
        vinyl.setDiscogsId(requestDto.discogsId());
        vinyl.setTitle(requestDto.title());
        vinyl.setLikesCount(0L);
        vinyl.setReviewsCount(0L);
        vinyl.setArtistsSort(requestDto.artistsSort());
        vinyl.setReleasedFormatted(requestDto.releasedFormatted());
        vinyl.setUser(user);

        return vinyl;
    }

    @Override
    public VinylDetailResponse getVinylDetail(Long discogsId, User currentUser) {
        // 실시간 필드: Redis 캐시 확인 -> 없으면 Discogs 호출
        DiscogsReleaseDto release = vinylDetailCacheService.getReleaseDetail(discogsId); // redis

        var vinylEntity = vinylRepository.findByDiscogsId(discogsId)
            .orElseThrow(() -> new RuntimeException(Constants.ALBUM_NOT_FOUND));

        boolean isLiking = likeRepository.findByUserAndVinyl(currentUser, vinylEntity).isPresent();
        boolean isListened = userVinylStatusRepository.findByUserAndVinyl(currentUser, vinylEntity)
            .map(UserVinylStatus::isListened)
            .orElse(false);

        // redis/실시간 호출 데이터 + db 메타데이터 합쳐 반환
        return VinylDetailResponse.builder()
                .release(release)
                .likesCount(vinylEntity.getLikesCount())
                .reviewsCount(vinylEntity.getReviewsCount())
                .isLiking(isLiking)
                .isListened(isListened)
                .build();
    }
}
