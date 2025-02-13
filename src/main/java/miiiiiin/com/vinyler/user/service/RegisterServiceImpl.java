package miiiiiin.com.vinyler.user.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.exception.user.UserAlreadyExistException;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final UserRepository userRepository;

//    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public User registerUser(ServiceRegisterDto dto) {
        // email 중복체크
        userRepository.findByEmail(dto.getEmail()).ifPresent(user -> {
            throw new UserAlreadyExistException(user.getEmail());
        });

        // nickname 중복체크
        userRepository.findByNickname(dto.getNickname()).ifPresent(user -> {
            throw new UserAlreadyExistException(user.getNickname());
        });


//        User user = dto.toEntity(passwordEncoder);

//        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
//        userRepository.save(user);
        User user = userRepository.save(User.of(dto.getEmail(), dto.getPassword(), dto.getNickname(), dto.getProfile(), dto.getBirthday()));
        return user;
    }
}
