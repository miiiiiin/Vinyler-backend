package miiiiiin.com.vinyler.user.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.application.dto.VinylDto;
import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.application.service.UserVinylStatusService;
import miiiiiin.com.vinyler.security.UserDetailsImpl;
import miiiiiin.com.vinyler.user.dto.ServiceRegisterDto;
import miiiiiin.com.vinyler.user.dto.request.ClientRegisterReqeustDto;
import miiiiiin.com.vinyler.user.dto.request.LoginRequestBody;
import miiiiiin.com.vinyler.user.dto.response.LoginResponseDto;
import miiiiiin.com.vinyler.user.dto.response.UserResponseDto;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원가입 엔드포인트")
public class UserController {

    private final UserService userService;
    private final UserVinylStatusService userVinylStatusService;


    @PostMapping("/register")
    @Operation(description = "신규 회원가입")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid ClientRegisterReqeustDto dto) {
        var response = userService.registerUser(ServiceRegisterDto.of(dto.getEmail(), dto.getPassword(), dto.getNickname(), dto.getProfile(), dto.getBirthday()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(description = "로그인")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestBody requestBody) {
        var response = userService.login(requestBody);
        return ResponseEntity.ok(response);
    }

    /**
     * 유저별 찜한 음반 리스트 조회
     */
    @GetMapping("/{userId}/liked")
    @Operation(description = "유저별 찜한 음반 리스트 조회")
    public ResponseEntity<List<VinylDto>> getVinylsLikedByUser(@PathVariable Long userId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var response = userService.getVinylsLikedByUser(userId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    /**
     * 유저별 감상한 음반 리스트 조회
     */
//    @GetMapping("/{userId}/liked")
//    @Operation(description = "유저별 찜한 음반 리스트 조회")
//    public ResponseEntity<List<VinylDto>> getVinylsListenedByUser(@PathVariable Long userId,
//                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        var response = userService.getListenedVinyls()
//        return ResponseEntity.ok(response);
//    }
}
