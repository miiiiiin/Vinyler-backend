package miiiiiin.com.vinyler.user.dto;

import lombok.Builder;
import lombok.Getter;
//import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Builder
public class ServiceRegisterDto {
    private String email;
    private String password;
    private String passwordConfirm;
    private String nickname;

    public boolean isPasswordConfirm() {
        return this.password.equals(this.passwordConfirm);
    }

    public static ServiceRegisterDto of(String email, String password, String nickname) {
        return ServiceRegisterDto.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
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
