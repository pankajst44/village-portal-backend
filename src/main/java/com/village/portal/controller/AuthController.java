package com.village.portal.controller;

import com.village.portal.dto.request.LoginRequest;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.AuthResponse;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.refreshToken(refreshToken, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        authService.logout(currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
