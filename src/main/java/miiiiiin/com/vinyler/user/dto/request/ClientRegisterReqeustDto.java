package miiiiiin.com.vinyler.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClientRegisterReqeustDto {
    @NotEmpty
    private String email;

    @NotEmpty
    private String password;

    @NotEmpty
    private String nickname;

    private String profile;

    @NotEmpty
    private LocalDate birthday;
}
