package miiiiiin.com.vinyler.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.auth.service.AuthService;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestDto;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증/로그인")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(description = "로그인")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        var response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
