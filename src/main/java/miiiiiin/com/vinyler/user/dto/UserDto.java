package miiiiiin.com.vinyler.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import miiiiiin.com.vinyler.user.entity.User;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Builder
@Data
public class UserDto {

    private Long id;
    private String email;
    private String nickname;
    private String profile;
    private LocalDate birthday;
    private Long followerCount;
    private Long followingCount;
    private ZonedDateTime createdDate;
    private ZonedDateTime modifiedDate;
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
