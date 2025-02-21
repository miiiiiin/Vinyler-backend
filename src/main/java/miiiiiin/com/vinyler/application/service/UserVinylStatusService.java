package miiiiiin.com.vinyler.application.service;

import miiiiiin.com.vinyler.application.dto.UserVinylStatusDto;
import miiiiiin.com.vinyler.application.dto.request.LikeRequestDto;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;

import java.util.List;

public interface UserVinylStatusService {
    UserVinylStatusDto toggleListenStatus(LikeRequestDto vinylRequestDto, User user);
    List<UserVinylStatus> getListenedVinyls(User user);
}
