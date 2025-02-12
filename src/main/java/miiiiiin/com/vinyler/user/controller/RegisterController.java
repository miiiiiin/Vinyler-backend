package miiiiiin.com.vinyler.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.user.dto.request.ClientRegisterReqeustDto;
import miiiiiin.com.vinyler.user.dto.request.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.service.RegisterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Register", description = "회원가입 엔드포인트")
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/register")
    @Operation(description = "신규 회원가입")
    public void registerUser(@RequestBody @Valid ClientRegisterReqeustDto dto) {
        registerService.registerUser(ServiceRegisterDto.of(dto.getEmail(), dto.getPassword(), dto.getNickname()));
    }

}
