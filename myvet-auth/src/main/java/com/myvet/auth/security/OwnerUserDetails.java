package com.myvet.auth.security;

import com.myvet.dataaccess.enums.Role;
import com.myvet.dataaccess.owner.Owner;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class OwnerUserDetails implements UserDetails {

    private final Owner owner;

    public Owner getOwner() {
        return owner;
    }

    public Integer getId() {
        return owner.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PET_OWNER"));
    }

    @Override
    public String getPassword() {
        return owner.getPassword();
    }

    @Override
    public String getUsername() {
        return owner.getEmail();
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
        return owner.isEnabled();
    }

    public Role getRole() {
        return Role.PET_OWNER;
    }
}
