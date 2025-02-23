package miiiiiin.com.vinyler.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.entity.Like;
import miiiiiin.com.vinyler.application.entity.UserVinylStatus;
import miiiiiin.com.vinyler.application.repository.LikeRepository;
//import miiiiiin.com.vinyler.application.repository.ReviewRepository;
import miiiiiin.com.vinyler.application.repository.UserVinylStatusRepository;
import miiiiiin.com.vinyler.auth.service.JwtService;
import miiiiiin.com.vinyler.exception.user.UserAlreadyExistException;
import miiiiiin.com.vinyler.exception.user.UserNotFoundException;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final UserVinylStatusRepository userVinylStatusRepository;

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
    public LoginResponseDto login(LoginRequestDto requestBody) {
        var userEntity = getUserEntity(requestBody.getEmail());

        if (passwordEncoder.matches(requestBody.getPassword(), userEntity.getPassword())) {
            var accessToken = jwtService.generateAccessToken(UserDetailsImpl.from(userEntity));
            return new LoginResponseDto(accessToken);
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    @Transactional
    public List<VinylDto> getVinylsLikedByUser(Long userId, User currentUser) {
        var userEntity = getUserEntity(userId, currentUser.getEmail());

        // LikeRepository를 통해 유저가 찜한 음반 목록 조회
        List<Like> likedVinyls = likeRepository.findByUser(userEntity);
        return likedVinyls.stream().map(VinylDto::of).toList();
    }

    @Override
    @Transactional
    public List<VinylDto> getVinylsListenedByUser(Long userId, User currentUser) {
        var userEntity = getUserEntity(userId, currentUser.getEmail());
        List<UserVinylStatus> listenedVinyls = userVinylStatusRepository.findByUserAndListened(userEntity, true);
        // TODO: FIX (USER ID?)
        return listenedVinyls.stream().map(VinylDto::of).toList();
    }

    private User getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private User getUserEntity(Long userId, String email) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
