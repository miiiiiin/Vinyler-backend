package miiiiiin.com.vinyler.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.request.ClientRegisterReqeustDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원가입 엔드포인트")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(description = "신규 회원가입")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid ClientRegisterReqeustDto dto) {
        User user = userService.registerUser(ServiceRegisterDto.of(dto.getEmail(), dto.getPassword(), dto.getNickname(), dto.getProfile(), dto.getBirthday()));
        var response = UserResponseDto.from(user);
        return ResponseEntity.ok(response);
    }
}
