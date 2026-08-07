package com.bondtradex.auth.security;

import com.bondtradex.auth.cache.CachedAuthorizationLoader;
import com.bondtradex.auth.dto.cache.CachedUserAuthorization;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachedUserDetailsService
        implements UserDetailsService {

    private final CachedAuthorizationLoader authorizationLoader;

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        CachedUserAuthorization authorization =
                authorizationLoader.load(username);

        List<GrantedAuthority> grantedAuthorities =
                authorization.authorities()
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .map(GrantedAuthority.class::cast)
                        .toList();

        return User.builder()
                .username(authorization.username())
                .password(authorization.encodedPassword())
                .disabled(!authorization.enabled())
                .accountExpired(
                        !authorization.accountNonExpired()
                )
                .accountLocked(
                        !authorization.accountNonLocked()
                )
                .credentialsExpired(
                        !authorization.credentialsNonExpired()
                )
                .authorities(grantedAuthorities)
                .build();
    }
}