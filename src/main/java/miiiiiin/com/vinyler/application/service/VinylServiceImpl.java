package miiiiiin.com.vinyler.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VinylServiceImpl implements VinylService {

    private final VinylRepository vinylRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public VinylLikeDto toggleLike(Long vinylId, User currentUser) {
        var vinylEntity = getVinyl(vinylId);
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

    private Vinyl getVinyl(Long vinylId) {
        return vinylRepository.findById(vinylId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vinyl Info Not Found"));
    }
}
