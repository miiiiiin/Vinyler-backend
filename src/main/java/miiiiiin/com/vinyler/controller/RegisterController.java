package miiiiiin.com.vinyler.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/signup")
@RequiredArgsConstructor
@Tag(name = "SignUp", description = "회원가입 엔드포인트")
public class SignUpController {

//    @Operation(summary = "signup POST", description = "이메일, 비밀번호 이용하여 회원가입")
//    public void registerUser(@RequestBody @Valid ) {
//
//    }
}
