package miiiiiin.com.vinyler.exception.follow;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import miiiiiin.com.vinyler.user.dto.UserDto;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.http.HttpStatus;

public class FollowNotFoundException extends ClientErrorException {

    public FollowNotFoundException() {
        super(HttpStatus.NOT_FOUND, "FOLLOW NOT FOUND");
    }

    // id는 모르지만 구체적인 메시지를 남기고 싶을 경우
    public FollowNotFoundException(User follower, User following) {
        super(HttpStatus.NOT_FOUND, "Follow with follower " + follower.getEmail() + " and follower" +
                following.getEmail() + "NOT FOUND");
    }
}
