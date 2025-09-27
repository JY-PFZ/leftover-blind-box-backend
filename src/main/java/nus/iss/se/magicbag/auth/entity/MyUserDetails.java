package nus.iss.se.magicbag.auth.entity;

import nus.iss.se.magicbag.auth.common.UserContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public record MyUserDetails(UserContext userContext, String password) implements UserDetails {
    //返回权限信息
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userContext.getRole().toUpperCase()));
    }

    //返回用户密码
    @Override
    public String getPassword() {
        return this.password;
    }

    //返回用户账号
    @Override
    public String getUsername() {
        return userContext.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
