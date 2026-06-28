package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

public interface VinylService {
    VinylLikeDto toggleLike(LikeRequestDto requestDto, User user);
    VinylLikeDto getLikeStatus(Long discogsId, User currentUser);
    Vinyl findOrCreateVinyl(LikeRequestDto requestDto, User user);
}
