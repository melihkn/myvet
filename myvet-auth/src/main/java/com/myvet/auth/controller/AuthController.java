// myvet-auth/src/main/java/com/myvet/auth/controller/AuthController.java
package com.myvet.auth.controller;

import com.myvet.auth.dto.request.LoginRequest;
import com.myvet.auth.dto.request.OwnerRegisterRequest;
import com.myvet.auth.dto.request.RefreshTokenRequest;
import com.myvet.auth.dto.request.VetRegisterRequest;
import com.myvet.auth.dto.response.AuthResponse;
import com.myvet.auth.service.AuthService;
import com.myvet.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new Veterinarian
     * POST /api/auth/register/vet
     */
    @PostMapping("/register/vet")
    public ResponseEntity<ApiResponse<AuthResponse>> registerVet(@Valid @RequestBody VetRegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerVet(request)));
    }

    /**
     * Register a new Pet Owner
     * POST /api/auth/register/owner
     */
    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<AuthResponse>> registerOwner(@Valid @RequestBody OwnerRegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerOwner(request)));
    }

    /**
     * Login - works for both Vet and Owner
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request)));
    }
}