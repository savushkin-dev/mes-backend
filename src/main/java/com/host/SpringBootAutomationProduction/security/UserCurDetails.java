package com.host.SpringBootAutomationProduction.security;


import com.host.SpringBootAutomationProduction.model.AuthType;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserCurDetails implements org.springframework.security.core.userdetails.UserDetails {

    private final User user;


    public UserCurDetails(User user) {
        this.user = user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Преобразуем роли пользователя в GrantedAuthority
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        // Для NTLM пользователей возвращаем null
        return user.getAuthType() == AuthType.NTLM ? null : user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
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

    public User getPerson(){
        return this.user;
    }
}
