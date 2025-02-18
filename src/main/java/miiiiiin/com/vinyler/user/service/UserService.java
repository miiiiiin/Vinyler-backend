package miiiiiin.com.vinyler.user.service;

import jakarta.validation.Valid;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestBody;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserResponseDto registerUser(ServiceRegisterDto dto);

    LoginResponseDto login(@Valid LoginRequestBody requestBody);
}
