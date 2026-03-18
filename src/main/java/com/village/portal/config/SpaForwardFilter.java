package com.village.portal.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Supports browser refresh on Angular client-side routes (e.g. /dashboard).
 *
 * If a request is a GET for a non-API path without a file extension and the client
 * accepts HTML, forward it to /index.html so Angular can handle routing.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SpaForwardFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldForwardToSpa(request, path)) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldForwardToSpa(HttpServletRequest request, String path) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) return false;

        if (path == null || path.isBlank()) return false;
        if ("/".equals(path) || "/index.html".equals(path)) return false;

        // Never forward API calls or actuator endpoints
        if (path.equals("/api") || path.startsWith("/api/")) return false;
        if (path.equals("/actuator") || path.startsWith("/actuator/")) return false;

        // Don't forward static assets (anything with a dot in the last segment)
        int lastSlash = path.lastIndexOf('/');
        String lastSegment = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        if (lastSegment.contains(".")) return false;

        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains("text/html");
    }
}

