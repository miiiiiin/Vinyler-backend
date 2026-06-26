package miiiiiin.com.vinyler.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.user.entity.User;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Builder
@Data
@Schema(description = "유저 정보")
public class UserDto {

    @Schema(description = "유저 ID", example = "1")
    private Long id;

    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "바이닐러")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profile;

    @Schema(description = "생년월일", example = "1995-06-15")
    private LocalDate birthday;

    @Schema(description = "팔로워 수", example = "42")
    private Long followerCount;

    @Schema(description = "팔로잉 수", example = "30")
    private Long followingCount;

    @Schema(description = "계정 생성일시")
    private ZonedDateTime createdDate;

    @Schema(description = "계정 수정일시")
    private ZonedDateTime modifiedDate;

    @Schema(description = "현재 사용자가 팔로우 중인지 여부", example = "false")
    private boolean isFollowing;

    public static UserDto from(User user, boolean isFollowing) {
        return UserDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profile(user.getProfile())
                .followerCount(user.getFollowersCount())
                .followingCount(user.getFollowingsCount())
                .createdDate(user.getCreatedDate())
                .modifiedDate(user.getModifiedDate())
                .isFollowing(isFollowing)
                .build();
    }
}
