package miiiiiin.com.vinyler.user.service;

import jakarta.validation.Valid;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestBody;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserResponseDto registerUser(ServiceRegisterDto dto);

    LoginResponseDto login(@Valid LoginRequestBody requestBody);

    List<VinylDto> getVinylsLikedByUser(Long userId, User user);

}
