package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.user.entity.User;

public interface VinylService {
    VinylLikeDto toggleLike(Long vinylId, User currentUser);
}
