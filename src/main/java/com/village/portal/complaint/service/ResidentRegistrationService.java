package com.village.portal.complaint.service;

import com.village.portal.complaint.dto.request.OtpVerifyRequest;
import com.village.portal.complaint.dto.request.ResidentRegisterRequest;
import com.village.portal.dto.response.AuthResponse;

import javax.servlet.http.HttpServletRequest;

public interface ResidentRegistrationService {
    AuthResponse register(ResidentRegisterRequest request, HttpServletRequest httpRequest);
    void sendOtp(String phone, Long userId, String ipAddress);
    void verifyOtp(OtpVerifyRequest request, Long userId);
    boolean isPhoneVerified(Long userId);
}
