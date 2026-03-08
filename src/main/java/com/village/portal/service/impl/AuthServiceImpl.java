package com.village.portal.service.impl;

import com.village.portal.dto.request.LoginRequest;
import com.village.portal.dto.response.AuthResponse;
import com.village.portal.entity.RefreshToken;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.exception.BusinessException;
import com.village.portal.repository.RefreshTokenRepository;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.JwtTokenProvider;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.AuditLogService;
import com.village.portal.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           RefreshTokenRepository refreshTokenRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuditLogService auditLogService) {
        this.authenticationManager    = authenticationManager;
        this.jwtTokenProvider         = jwtTokenProvider;
        this.refreshTokenRepository   = refreshTokenRepository;
        this.userRepository           = userRepository;
        this.passwordEncoder          = passwordEncoder;
        this.auditLogService          = auditLogService;
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken     = jwtTokenProvider.generateAccessToken(authentication);
        String rawRefreshToken = UUID.randomUUID().toString();

        // SHA-256 hash -- deterministic, so we can look it up later by exact match.
        // BCrypt is NOT used here because BCrypt produces a different hash every call,
        // making findByTokenHash impossible without a full table scan.
        String tokenHash = sha256(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000));
        refreshToken.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        refreshToken.setIpAddress(extractClientIp(httpRequest));
        refreshTokenRepository.save(refreshToken);

        auditLogService.log("users", user.getId(),
                AuditAction.LOGIN, null, null,
                "User logged in: " + user.getUsername());

        return buildAuthResponse(accessToken, rawRefreshToken, user);
    }

    // ── REFRESH TOKEN ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(String rawRefreshToken, HttpServletRequest httpRequest) {

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BusinessException("INVALID_REFRESH_TOKEN",
                    "Refresh token must not be blank");
        }

        // Hash the incoming raw token to look it up in the database
        String tokenHash = sha256(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN",
                        "Refresh token not found or has already been used"));

        // Reject revoked tokens
        if (Boolean.TRUE.equals(stored.getIsRevoked())) {
            throw new BusinessException("REFRESH_TOKEN_REVOKED",
                    "Refresh token has been revoked. Please log in again.");
        }

        // Reject expired tokens
        if (LocalDateTime.now().isAfter(stored.getExpiresAt())) {
            stored.setIsRevoked(true);
            refreshTokenRepository.save(stored);
            throw new BusinessException("REFRESH_TOKEN_EXPIRED",
                    "Refresh token has expired. Please log in again.");
        }

        User user = stored.getUser();

        // Reject tokens belonging to disabled accounts
        if (!user.getIsActive()) {
            stored.setIsRevoked(true);
            refreshTokenRepository.save(stored);
            throw new BusinessException("ACCOUNT_DISABLED",
                    "Your account has been deactivated. Please contact an administrator.");
        }

        // Rotate: revoke the old token and issue a fresh one (prevents replay attacks)
        stored.setIsRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken     = jwtTokenProvider.generateAccessTokenFromUsername(
                user.getUsername(), user.getRole().name());
        String newRawRefreshToken = UUID.randomUUID().toString();
        String newTokenHash       = sha256(newRawRefreshToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setTokenHash(newTokenHash);
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000));
        newRefreshToken.setDeviceInfo(httpRequest.getHeader("User-Agent"));
        newRefreshToken.setIpAddress(extractClientIp(httpRequest));
        refreshTokenRepository.save(newRefreshToken);

        return buildAuthResponse(newAccessToken, newRawRefreshToken, user);
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenRepository.revokeAllUserTokens(user.getId());
            auditLogService.log("users", user.getId(),
                    AuditAction.LOGOUT, null, null,
                    "User logged out: " + username);
        });
        SecurityContextHolder.clearContext();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * SHA-256 hex digest of the raw token string.
     * Deterministic (same input -> same output), so we can store it once and
     * look it up later with a plain findByTokenHash query.
     * SHA-256 is safe here because refresh tokens are high-entropy random UUIDs;
     * BCrypt salting is only needed for low-entropy secrets like passwords.
     */
    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present in every JVM (JCA spec)
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private AuthResponse buildAuthResponse(String accessToken, String rawRefreshToken, User user) {
        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                accessTokenExpiryMs / 1000,
                user.getUsername(),
                user.getRole().name(),
                user.getFullName()
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank())
                ? xff.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}