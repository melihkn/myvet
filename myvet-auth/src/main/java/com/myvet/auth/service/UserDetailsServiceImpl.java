// myvet-auth/src/main/java/com/myvet/auth/service/UserDetailsServiceImpl.java
package com.myvet.auth.service;

import com.myvet.dataaccess.repository.OwnerRepository;
import com.myvet.dataaccess.repository.VetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final OwnerRepository ownerRepository;
    private final VetRepository vetRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try Owner first, then Vet
        Optional<? extends UserDetails> user = ownerRepository.findByEmail(username)
                .map(o -> (UserDetails) o);

        if (user.isEmpty()) {
            user = vetRepository.findByEmail(username)
                    .map(v -> (UserDetails) v);
        }

        return user.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}