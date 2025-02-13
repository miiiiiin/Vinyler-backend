package miiiiiin.com.vinyler.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
//import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Builder
public class ServiceRegisterDto {
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;
    private LocalDate birthday;
    private String profile;

    public boolean isPasswordConfirm() {
        return this.password.equals(this.passwordConfirm);
    }

    public static ServiceRegisterDto of(
            String email,
            String password,
            String nickname,
            String profile,
            LocalDate birthday) {
        return ServiceRegisterDto.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .profile(profile)
                .birthday(birthday)
                .build();
    }
//
//    public User toEntity(PasswordEncoder passwordEncoder) {
//        return User.builder()
//                .email(this.email)
//                .nickname(this.nickname)
//                .password(passwordEncoder.encode(this.password))
//                .build();
//    }
}
