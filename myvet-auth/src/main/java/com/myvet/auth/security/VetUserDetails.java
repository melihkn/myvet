package com.myvet.auth.security;

import com.myvet.dataaccess.enums.Role;
import com.myvet.dataaccess.vet.Vet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class VetUserDetails implements UserDetails {

    private final Vet vet;

    public Vet getVet() {
        return vet;
    }

    public Integer getId() {
        return vet.getVid();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_VET"));
    }

    @Override
    public String getPassword() {
        return vet.getPassword();
    }

    @Override
    public String getUsername() {
        return vet.getEmail();
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

    public Role getRole() {
        return Role.VET;
    }
}
