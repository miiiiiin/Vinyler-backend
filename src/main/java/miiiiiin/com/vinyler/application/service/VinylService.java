package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.VinylDetailDto;
import miiiiiin.com.vinyler.application.dto.VinylLikeDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.dto.response.VinylDetailResponse;
import miiiiiin.com.vinyler.application.entity.vinyl.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

public interface VinylService {
    VinylLikeDto toggleLike(LikeRequestDto requestDto, User user);
    VinylDetailResponse getVinylDetail(Long discogsId, User currentUser);
    Vinyl findOrCreateVinyl(LikeRequestDto requestDto, User user);
}
