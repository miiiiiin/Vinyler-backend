package miiiiiin.com.vinyler.user.oauth;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import miiiiiin.com.vinyler.user.entity.User;

@AllArgsConstructor
public class SocialOAuth2User implements OAuth2User {
    private User user;
    private Map<String, Object> attributeMap;

    @Override
    public Map<String, Object> getAttributes() {
        return attributeMap;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getName() {
        return this.user.getNickname();
    }

    public User getUser() {
        return this.user;
    }
}
