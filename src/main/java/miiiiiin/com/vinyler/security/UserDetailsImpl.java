package miiiiiin.com.vinyler.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miiiiiin.com.vinyler.auth.service.JwtService;
import miiiiiin.com.vinyler.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private final User user;
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);


    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        logger.info("getusername is set: {}" + user.getUsername());  // 디
        return user.getUsername();
    }
}
