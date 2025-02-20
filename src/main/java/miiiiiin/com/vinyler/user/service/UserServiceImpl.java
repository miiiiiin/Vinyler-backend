package miiiiiin.com.vinyler.user.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
import miiiiiin.com.vinyler.application.repository.VinylRepository;
import miiiiiin.com.vinyler.auth.service.JwtService;
import miiiiiin.com.vinyler.exception.user.UserAlreadyExistException;
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestBody;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VinylRepository vinylRepository;
    private final LikeRepository likeRepository;

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserEntity(username);
        return new UserDetailsImpl(user);
    }

    @Override
    public UserResponseDto registerUser(ServiceRegisterDto dto) {
        // email 중복체크
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            throw new UserAlreadyExistException(user.getEmail());
        });

        // nickname 중복체크
        userRepository.findByNickname(dto.getNickname()).ifPresent(user -> {
            throw new UserAlreadyExistException(user.getNickname());
        });

        dto.setPasword(passwordEncoder);
        User user = userRepository.save(dto.toEntity());
        return UserResponseDto.from(user);
    }

    @Override
    public LoginResponseDto login(LoginRequestBody requestBody) {
        var userEntity = getUserEntity(requestBody.getEmail());

        if (passwordEncoder.matches(requestBody.getPassword(), userEntity.getPassword())) {
            var accessToken = jwtService.generateAccessToken(userEntity);
            return new LoginResponseDto(accessToken);
        } else {
            throw new UserNotFoundException();
        }
    }

    private User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

    }
}
