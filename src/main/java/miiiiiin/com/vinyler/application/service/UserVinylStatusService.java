package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.UserVinylStatusDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.user.entity.User;

public interface UserVinylStatusService {
    UserVinylStatusDto toggleListenStatus(LikeRequestDto vinylRequestDto, User user);
}
