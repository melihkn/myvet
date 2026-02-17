// myvet-auth/src/main/java/com/myvet/auth/service/AuthService.java
package com.myvet.auth.service;

import com.myvet.auth.dto.request.LoginRequest;
import com.myvet.auth.dto.request.OwnerRegisterRequest;
import com.myvet.auth.dto.request.RefreshTokenRequest;
import com.myvet.auth.dto.request.VetRegisterRequest;
import com.myvet.auth.dto.response.AuthResponse;
import com.myvet.auth.security.OwnerUserDetails;
import com.myvet.auth.security.VetUserDetails;
import com.myvet.dataaccess.enums.Role;
import com.myvet.dataaccess.owner.Owner;
import com.myvet.dataaccess.owner.OwnerRepository;
import com.myvet.dataaccess.vet.VetRepository;
import com.myvet.dataaccess.vet.Vet;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new Veterinarian
     */
    @Transactional
    public AuthResponse registerVet(VetRegisterRequest request) {
        // Check if email already exists
        if (vetRepository.existsByEmail(request.getEmail()) || ownerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if license number already exists
        if (vetRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number already registered");
        }

        // Create vet directly (no separate User entity)
        Vet vet = Vet.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .specialization(request.getSpecialization())
                .bio(request.getBio())
                .build();

        vet.setCreatedAt(LocalDateTime.now());
        vet.setUpdatedAt(LocalDateTime.now());
        vetRepository.save(vet);

        // Create wrapper and generate tokens
        VetUserDetails vetUserDetails = new VetUserDetails(vet);
        String accessToken = jwtService.generateAccessToken(vetUserDetails);
        String refreshToken = jwtService.generateRefreshToken(vetUserDetails);

        return buildAuthResponse(vet.getVid(), vet.getEmail(), vet.getFirstName(), vet.getLastName(), Role.VET, accessToken, refreshToken);
    }

    /**
     * Register a new Pet Owner
     */
    @Transactional
    public AuthResponse registerOwner(OwnerRegisterRequest request) {
        // Check if email already exists
        if (ownerRepository.existsByEmail(request.getEmail()) || vetRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if TC already exists
        if (ownerRepository.existsByTc(request.getTc())) {
            throw new RuntimeException("TC Kimlik number already registered");
        }

        // Create owner directly (no separate User entity)
        Owner owner = Owner.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .tc(request.getTc())
                .address(request.getAddress())
                .enabled(true)
                .build();

        owner.setUpdatedAt(LocalDateTime.now());
        owner.setCreatedAt(LocalDateTime.now());

        ownerRepository.save(owner);

        // Create wrapper and generate tokens
        OwnerUserDetails ownerUserDetails = new OwnerUserDetails(owner);
        String accessToken = jwtService.generateAccessToken(ownerUserDetails);
        String refreshToken = jwtService.generateRefreshToken(ownerUserDetails);

        return buildAuthResponse(owner.getId(), owner.getEmail(), owner.getFirstName(), owner.getLastName(), Role.PET_OWNER, accessToken, refreshToken);
    }

    /**
     * Login - works for both Vet and Owner
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Try to find the authenticated entity (Owner or Vet)
        UserDetails userDetails = findUserByEmail(request.getEmail());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return buildAuthResponseFromUserDetails(userDetails, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtService.extractUsername(refreshToken);

        UserDetails userDetails = findUserByEmail(email);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return buildAuthResponseFromUserDetails(userDetails, newAccessToken, refreshToken);
    }

    /**
     * Find a user (Owner or Vet) by email and wrap in UserDetails
     */
    private UserDetails findUserByEmail(String email) {
        // Try Vet first
        var vet = vetRepository.findByEmail(email);
        if (vet.isPresent()) {
            return new VetUserDetails(vet.get());
        }

        // Then try Owner
        var owner = ownerRepository.findByEmail(email);
        if (owner.isPresent()) {
            return new OwnerUserDetails(owner.get());
        }

        throw new RuntimeException("User not found with email: " + email);
    }

    private AuthResponse buildAuthResponseFromUserDetails(UserDetails userDetails, String accessToken, String refreshToken) {
        if (userDetails instanceof OwnerUserDetails ownerDetails) {
            Owner owner = ownerDetails.getOwner();
            return buildAuthResponse(owner.getId(), owner.getEmail(), owner.getFirstName(), owner.getLastName(), Role.PET_OWNER, accessToken, refreshToken);
        } else if (userDetails instanceof VetUserDetails vetDetails) {
            Vet vet = vetDetails.getVet();
            return buildAuthResponse(vet.getVid(), vet.getEmail(), vet.getFirstName(), vet.getLastName(), Role.VET, accessToken, refreshToken);
        }
        throw new RuntimeException("Unknown user type");
    }

    private AuthResponse buildAuthResponse(Integer id, String email, String firstName, String lastName, Role role, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(id)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .role(role)
                        .build())
                .build();
    }
}

