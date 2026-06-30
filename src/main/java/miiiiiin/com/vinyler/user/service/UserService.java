package miiiiiin.com.vinyler.user.service;

import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.dto.response.SliceResponse;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.dto.request.UpdateUserRequestDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserResponseDto registerUser(ServiceRegisterDto dto);
    SliceResponse<VinylDto> getVinylsLikedByUser(Long userId, User user, Long cursorId, int size);
    SliceResponse<VinylDto> getVinylsListenedByUser(Long userId, User user, Long cursorId, int size);
    UserDto follow(Long userId, User user);
    UserDto unfollow(Long userId, User user);
    List<UserDto> getFollowersByUser(Long userId, User user);
    List<UserDto> getFollowingsByUser(Long userId, User user);
    UserDto getUserInfo(User user);
    UserDto updateUserInfo(User currentUser, UpdateUserRequestDto dto);

    void withdrawUser(User currentUser);

}
