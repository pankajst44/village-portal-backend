package com.village.portal.complaint.controller;

import com.village.portal.complaint.dto.request.OtpVerifyRequest;
import com.village.portal.complaint.dto.request.ResidentRegisterRequest;
import com.village.portal.complaint.service.ResidentRegistrationService;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.AuthResponse;
import com.village.portal.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

@RestController
@RequestMapping("/auth")
public class ResidentAuthController {

    private final ResidentRegistrationService registrationService;

    public ResidentAuthController(ResidentRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /** Public — any visitor can register as a resident. */
    @PostMapping("/register/resident")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody ResidentRegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = registrationService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Please verify your phone.", response));
    }

    /**
     * Send OTP to resident's phone.
     * Accessible to authenticated residents only (they are logged in but phone not yet verified).
     */
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @RequestParam @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
            String phone,
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getHeader("X-Forwarded-For") != null
                ? httpRequest.getHeader("X-Forwarded-For").split(",")[0].trim()
                : httpRequest.getRemoteAddr();

        registrationService.sendOtp(phone, currentUser.getId(), ip);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your registered phone number."));
    }

    /** Verify OTP — marks phone as verified and unlocks complaint submission. */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        registrationService.verifyOtp(request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Phone number verified successfully."));
    }

    /** Check phone verification status. */
    @GetMapping("/otp/status")
    public ResponseEntity<ApiResponse<Boolean>> getVerificationStatus(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        boolean verified = registrationService.isPhoneVerified(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(verified));
    }
}
