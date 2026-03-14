package com.village.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider        jwtTokenProvider;
    private final UserDetailsServiceImpl  userDetailsService;
    private final ObjectMapper            objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // Token is valid — load user and set authentication
                    String      username    = jwtTokenProvider.getUsernameFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                // validateToken() returned false for a non-expiry reason (bad signature etc.)
                // → fall through, Spring Security will block as anonymous on protected routes

            } catch (ExpiredJwtException e) {
                // Token was valid but has expired — tell the client explicitly so it
                // can use its refresh token. Do NOT fall through to the filter chain
                // for protected endpoints, or Spring will return 403 (anonymous user
                // forbidden) instead of the 401 the Angular interceptor needs.
                sendUnauthorized(response, "TOKEN_EXPIRED", "Access token has expired");
                return;  // ← short-circuit: do not continue the filter chain
            }
        }

        filterChain.doFilter(request, response);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void sendUnauthorized(HttpServletResponse response,
                                  String code,
                                  String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);          // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of(
                "success",   false,
                "errorCode", code,
                "message",   message,
                "timestamp", Instant.now().toString()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}