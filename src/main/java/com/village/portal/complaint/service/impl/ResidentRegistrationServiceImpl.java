package com.village.portal.complaint.service.impl;

import com.village.portal.complaint.dto.request.OtpVerifyRequest;
import com.village.portal.complaint.dto.request.ResidentRegisterRequest;
import com.village.portal.complaint.entity.ResidentVerification;
import com.village.portal.complaint.repository.ResidentVerificationRepository;
import com.village.portal.complaint.service.ResidentRegistrationService;
import com.village.portal.complaint.service.SmsService;
import com.village.portal.constants.AppConstants;
import com.village.portal.dto.response.AuthResponse;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.enums.Role;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.DuplicateResourceException;
import com.village.portal.repository.RefreshTokenRepository;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.JwtTokenProvider;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Service
public class ResidentRegistrationServiceImpl implements ResidentRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(ResidentRegistrationServiceImpl.class);

    private final UserRepository                userRepository;
    private final ResidentVerificationRepository verificationRepository;
    private final PasswordEncoder               passwordEncoder;
    private final JwtTokenProvider              jwtTokenProvider;
    private final RefreshTokenRepository        refreshTokenRepository;
    private final AuditLogService               auditLogService;
    private final SmsService smsService;

    public ResidentRegistrationServiceImpl(
            UserRepository userRepository,
            ResidentVerificationRepository verificationRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            AuditLogService auditLogService,
            SmsService smsService) {
        this.userRepository        = userRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder       = passwordEncoder;
        this.jwtTokenProvider      = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogService       = auditLogService;
        this.smsService            = smsService;
    }

    // ── Register ─────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(ResidentRegisterRequest request, javax.servlet.http.HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        // Phone uniqueness — one phone per resident account
        verificationRepository.findByPhoneNumber(request.getPhone()).ifPresent(existing -> {
            if (existing.getIsVerified()) {
                throw new DuplicateResourceException("Phone number is already registered to another account");
            }
        });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        // Residents use phone as email placeholder — no email required
        user.setEmail(request.getUsername() + "@resident.local");
        user.setPhone(request.getPhone());
        user.setRole(Role.RESIDENT);
        user.setIsActive(true);
        userRepository.save(user);

        auditLogService.log(AppConstants.TABLE_USERS, user.getId(),
                AuditAction.REGISTER, null, null,
                "Resident self-registration: " + user.getUsername());

        // Issue JWT immediately — resident can browse but not submit until OTP verified
        UserDetailsImpl principal = new UserDetailsImpl(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        String accessToken  = jwtTokenProvider.generateAccessToken(auth);
        String rawRefresh   = UUID.randomUUID().toString();
        saveRefreshToken(user, rawRefresh, httpRequest);

        return new AuthResponse(
                accessToken, rawRefresh,
                1800L,
                user.getUsername(), user.getRole().name(), user.getFullName());
    }

    // ── Send OTP ─────────────────────────────────────────────

    @Override
    @Transactional
    public void sendOtp(String phone, Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        // Check rate: max 5 OTP requests per user
        ResidentVerification verification = verificationRepository.findByUserId(userId)
                .orElse(new ResidentVerification());

        if (verification.getAttempts() != null && verification.getAttempts() >= AppConstants.OTP_MAX_ATTEMPTS) {
            throw new BusinessException("OTP_MAX_ATTEMPTS",
                    "Too many OTP attempts. Please contact admin.");
        }

        String rawOtp  = generateOtp();
        String otpHash = sha256(rawOtp);

        verification.setUser(user);
        verification.setPhoneNumber(phone);
        verification.setOtpHash(otpHash);
        verification.setOtpExpiresAt(LocalDateTime.now().plusMinutes(AppConstants.OTP_EXPIRY_MINUTES));
        verification.setIsVerified(false);
        verification.setAttempts(verification.getAttempts() == null ? 1
                : verification.getAttempts() + 1);
        verification.setIpAddress(ipAddress);
        verificationRepository.save(verification);

        // Send OTP via configured SMS provider
        // Dev: LogSmsServiceImpl prints to console (default, no config needed)
        // Prod: TwilioSmsServiceImpl sends real SMS (set app.sms.provider=twilio)
        String smsBody = buildOtpMessage(rawOtp);
        try {
            smsService.send(phone, smsBody);
        } catch (Exception e) {
            // SMS failure should not silently succeed — surface a clear error
            // OTP is already saved to DB so retry (re-send) will still work
            log.error("SMS delivery failed for user {} phone {}: {}", userId, maskPhone(phone), e.getMessage());
            throw new BusinessException("SMS_FAILED",
                    "Could not send OTP. Please check your phone number or try again in a few minutes.");
        }

        auditLogService.log(AppConstants.TABLE_USERS, userId,
                AuditAction.OTP_SENT, null, null, "OTP sent to " + maskPhone(phone));
    }

    // ── Verify OTP ────────────────────────────────────────────

    @Override
    @Transactional
    public void verifyOtp(OtpVerifyRequest request, Long userId) {
        ResidentVerification verification = verificationRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("OTP_NOT_FOUND",
                        "No OTP found. Please request a new OTP."));

        if (verification.getIsVerified()) {
            return; // already verified — idempotent
        }

        if (LocalDateTime.now().isAfter(verification.getOtpExpiresAt())) {
            throw new BusinessException("OTP_EXPIRED",
                    "OTP has expired. Please request a new one.");
        }

        if (!sha256(request.getOtp()).equals(verification.getOtpHash())) {
            throw new BusinessException("OTP_INVALID", "Incorrect OTP. Please try again.");
        }

        verification.setIsVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        verification.setOtpHash(null); // clear hash after use
        verificationRepository.save(verification);

        auditLogService.log(AppConstants.TABLE_USERS, userId,
                AuditAction.OTP_VERIFIED, null, null,
                "Phone verified: " + maskPhone(request.getPhone()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPhoneVerified(Long userId) {
        return verificationRepository.findByUserId(userId)
                .map(ResidentVerification::getIsVerified)
                .orElse(false);
    }

    // ── Helpers ───────────────────────────────────────────────

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private String buildOtpMessage(String otp) {
        return String.format(
                "Your Village Portal OTP is: %s. Valid for %d minutes. Do not share this with anyone.",
                otp, AppConstants.OTP_EXPIRY_MINUTES
        );
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return "******" + phone.substring(phone.length() - 4);
    }

    private void saveRefreshToken(User user, String rawToken,
                                  javax.servlet.http.HttpServletRequest httpRequest) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            String tokenHash = hex.toString();

            com.village.portal.entity.RefreshToken rt = new com.village.portal.entity.RefreshToken();
            rt.setUser(user);
            rt.setTokenHash(tokenHash);
            rt.setExpiresAt(LocalDateTime.now().plusDays(7));
            rt.setDeviceInfo(httpRequest.getHeader("User-Agent"));
            String xff = httpRequest.getHeader("X-Forwarded-For");
            rt.setIpAddress(xff != null ? xff.split(",")[0].trim() : httpRequest.getRemoteAddr());
            refreshTokenRepository.save(rt);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}