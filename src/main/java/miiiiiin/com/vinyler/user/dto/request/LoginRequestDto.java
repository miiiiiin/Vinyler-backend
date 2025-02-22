package miiiiiin.com.vinyler.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @NotEmpty
    String email;
    @NotEmpty
    String password;
}
