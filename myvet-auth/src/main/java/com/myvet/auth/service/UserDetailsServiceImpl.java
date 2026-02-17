// myvet-auth/src/main/java/com/myvet/auth/service/UserDetailsServiceImpl.java
package com.myvet.auth.service;

import com.myvet.auth.security.OwnerUserDetails;
import com.myvet.auth.security.VetUserDetails;
import com.myvet.dataaccess.owner.OwnerRepository;
import com.myvet.dataaccess.vet.VetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final OwnerRepository ownerRepository;
    private final VetRepository vetRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find as Vet first
        var vet = vetRepository.findByEmail(username);
        if (vet.isPresent()) {
            return new VetUserDetails(vet.get());
        }

        // Try to find as Owner
        var owner = ownerRepository.findByEmail(username);
        if (owner.isPresent()) {
            return new OwnerUserDetails(owner.get());
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}