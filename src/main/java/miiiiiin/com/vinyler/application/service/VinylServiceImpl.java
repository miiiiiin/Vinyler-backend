package miiiiiin.com.vinyler.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.global.Constants;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VinylServiceImpl implements VinylService {

    private final VinylRepository vinylRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public VinylLikeDto toggleLike(LikeRequestDto requestDto, User currentUser) {
        // Vinyl 엔티티가 DB에 존재하는지 확인, 없으면 저장
        var entity = Vinyl.of(requestDto, currentUser);

        var vinylEntity = vinylRepository.findByDiscogsId(entity.getDiscogsId())
                .orElseGet(() -> {
                    // 새로 저장할 때는 vinylId를 설정할 필요 없음 (자동 생성됨)
                    return vinylRepository.save(entity);
                });

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

    @Override
    public VinylLikeDto getLikeStatus(Long discogsId, User currentUser) {
        var vinylEntity = vinylRepository.findByDiscogsId(discogsId)
            .orElseThrow(() -> new RuntimeException(Constants.ALBUM_NOT_FOUND));

        // 사용자와 Vinyl에 대한 Like 조회
        var likeEntity = likeRepository.findByUserAndVinyl(currentUser, vinylEntity);

        if (likeEntity.isPresent()) {
            return VinylLikeDto.from(vinylEntity, currentUser, true);
        } else {
            return VinylLikeDto.from(vinylEntity, currentUser, false);
        }
    }
}
