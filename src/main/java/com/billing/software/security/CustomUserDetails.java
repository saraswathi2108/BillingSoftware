package com.billing.software.security;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.billing.software.entity.User;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private User user;  

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // FIX: User entity lo unna Role enum values ni GrantedAuthority objects ga convert chestunnam.
        return user.getRoles().stream()
                // Role enum nunchi name ni theesukuni GrantedAuthority ga create chestundhi.
                .map(role -> new SimpleGrantedAuthority(role.name())) 
                .collect(Collectors.toList());
    }
    @Override
    public String getPassword() {
        return user.getPassword();  // Reading from your User entity
    }

    @Override
    public String getUsername() {
        return user.getEmail();  // Email is username
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