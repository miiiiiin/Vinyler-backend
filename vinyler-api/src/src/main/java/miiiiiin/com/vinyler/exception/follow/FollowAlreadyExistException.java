package miiiiiin.com.vinyler.exception.follow;

import miiiiiin.com.vinyler.exception.ClientErrorException;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.http.HttpStatus;

public class FollowAlreadyExistException extends ClientErrorException{
    public FollowAlreadyExistException() {
        super(HttpStatus.CONFLICT, "FOLLOW ALREADY EXISTS");
    }

    // 예외가 발생했을 때, 구체적인 postid를 알고 있을 경우
    public FollowAlreadyExistException(User follower, User following) {
        super(HttpStatus.CONFLICT, "Follow with follower " + follower.getEmail() + " and follower" +
                following.getEmail() + "already exists");
    }
}
