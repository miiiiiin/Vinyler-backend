package miiiiiin.com.vinyler.user.service;

import jakarta.validation.Valid;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.dto.response.SliceResponse;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserResponseDto registerUser(ServiceRegisterDto dto);
    SliceResponse getVinylsLikedByUser(Long userId, User user, Long cursorId, int size);
    List<VinylDto> getVinylsListenedByUser(Long userId, User user);
    UserDto follow(Long userId, User user);
    UserDto unfollow(Long userId, User user);
    List<UserDto> getFollowersByUser(Long userId, User user);
    List<UserDto> getFollowingsByUser(Long userId, User user);
    UserDto getUserInfo(User user);
}
