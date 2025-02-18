package miiiiiin.com.vinyler.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class LoginRequestBody {
    @NotEmpty
    String email;
    @NotEmpty
    String password;
}
