package miiiiiin.com.vinyler.application.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDetailDto;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.global.Constants;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VinylServiceImpl implements VinylService {

    private final VinylRepository vinylRepository;
    private final LikeRepository likeRepository;
    private final UserVinylStatusRepository userVinylStatusRepository;

    @Override
    @Transactional
    public Vinyl findOrCreateVinyl(LikeRequestDto requestDto, User user) {
        return vinylRepository.findByDiscogsId(requestDto.discogsId())
                .orElseGet(() -> vinylRepository.save(buildVinylFromDto(requestDto, user)));
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
            return VinylLikeDto.from(vinylRepository.save(vinylEntity), currentUser, false);
        } else {
            likeRepository.save(Like.of(currentUser, vinylEntity));
            vinylEntity.setLikesCount(vinylEntity.getLikesCount() + 1);
            return VinylLikeDto.from(vinylRepository.save(vinylEntity), currentUser, true);
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
    public VinylDetailDto getVinylDetail(Long discogsId, User currentUser) {
        var vinylEntity = vinylRepository.findByDiscogsId(discogsId)
            .orElseThrow(() -> new RuntimeException(Constants.ALBUM_NOT_FOUND));

        boolean isLiking = likeRepository.findByUserAndVinyl(currentUser, vinylEntity).isPresent();
        boolean isListened = userVinylStatusRepository.findByUserAndVinyl(currentUser, vinylEntity)
            .map(UserVinylStatus::isListened)
            .orElse(false);

        return VinylDetailDto.builder()
            .vinylId(vinylEntity.getVinylId())
            .discogsId(vinylEntity.getDiscogsId())
            .likesCount(vinylEntity.getLikesCount())
            .reviewsCount(vinylEntity.getReviewsCount())
            .isLiking(isLiking)
            .isListened(isListened)
            .build();
    }
}
