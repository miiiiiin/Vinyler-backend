package miiiiiin.com.vinyler.user.service;

import lombok.RequiredArgsConstructor;
import miiiiiin.com.vinyler.user.entity.User;
import miiiiiin.com.vinyler.user.oauth.SocialOAuth2User;
import miiiiiin.com.vinyler.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SocialOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> map = (Map) oAuth2User.getAttributes().get("kakao_account");
        String email = (String) map.get("email");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerUser(map));

        return new SocialOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerUser(Map<String, Object> map) {
        User user = User.builder()
                .email(map.get("email").toString())
                .nickname((String) ((Map) map.get("profile")).get("nickname"))
                .profile((String) ((Map) map.get("profile")).get("profile_image_url"))
                .password("null")
                .birthday(getBirthDay(map))
                .build();
        return userRepository.save(user);
//        return user;
    }

    private LocalDate getBirthDay(Map<String, Object> map) {
        String birthyear = map.get("birthyear").toString();
        String birthday = map.get("birthday").toString();
        return LocalDate.parse(birthyear + birthday, DateTimeFormatter.BASIC_ISO_DATE);
    }
}
