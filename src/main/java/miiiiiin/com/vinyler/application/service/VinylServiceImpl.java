package miiiiiin.com.vinyler.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.exception.vinyl.VinylNotFoundException;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VinylServiceImpl implements VinylService {

    private final VinylRepository vinylRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public VinylLikeDto toggleLike(LikeRequestDto requestDto, User currentUser) {
        // Vinyl 엔티티가 DB에 존재하는지 확인, 없으면 저장
        var entity = requestDto.toEntity();
        var vinylEntity = getVinyl(requestDto.toEntity().getDiscogsId());
//        var vinylEntity = vinylRepository.findByDiscogsId(entity.getDiscogsId())
//                .orElseGet(() -> vinylRepository.save(entity));

        // 사용자와 Vinyl에 대한 Like 조회
        var likeEntity = likeRepository.findByUserAndVinyl(currentUser, vinylEntity);

        if (likeEntity.isPresent()) {
            likeRepository.delete(likeEntity.get());
            vinylEntity.setLikesCount(Math.max(0, vinylEntity.getLikesCount() - 1));
            return VinylLikeDto.from(vinylRepository.save(vinylEntity), false);
        } else {
            likeRepository.save(Like.of(currentUser, vinylEntity));
            vinylEntity.setLikesCount(vinylEntity.getLikesCount() + 1);
            return VinylLikeDto.from(vinylRepository.save(vinylEntity), true);
        }
    }

    private Vinyl getVinyl(Long discogsId) {
        /**
         * discogs 릴리스 ID 기준 찾기
         */
        return vinylRepository.findByDiscogsId(discogsId)
                .orElseThrow(() -> new VinylNotFoundException(discogsId));
    }
}
