package com.village.portal.service;

import com.village.portal.dto.request.LoginRequest;
import com.village.portal.dto.response.AuthResponse;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    AuthResponse refreshToken(String refreshToken, HttpServletRequest httpRequest);

    void logout(String username);
}
