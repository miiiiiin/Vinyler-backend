package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

public interface VinylService {
    VinylLikeDto toggleLike(LikeRequestDto vinylRequestDto, User currentUser);
}
