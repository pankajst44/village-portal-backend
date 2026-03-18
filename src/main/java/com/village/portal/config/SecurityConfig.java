package com.village.portal.config;

import com.village.portal.security.JwtAuthenticationFilter;
import com.village.portal.security.JwtTokenProvider;
import com.village.portal.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider   = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // ── CORS preflight ──
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ── Health endpoint for Railway/load balancers (must be before static files) ──
                .antMatchers("/actuator/**").permitAll()

                // ── SPA static files (Angular build in /static) ──
                .antMatchers(
                        "/",
                        "/index.html",
                        "/favicon.ico",
                        "/assets/**",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.map",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.webp",
                        "/**/*.woff",
                        "/**/*.woff2",
                        "/**/*.ttf"
                ).permitAll()

                // ── Auth ──
                .antMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/register/resident").permitAll()

                // ── Public portal — no auth required ──
                .antMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/projects/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/funds/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/contractors/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/documents/public/**").permitAll()
                // Document download: isPublic docs served to everyone
                .antMatchers(HttpMethod.GET, "/api/documents/*/download").permitAll()

                // ── Complaint routes (added for CMS — Phase 2) ──
                .antMatchers(HttpMethod.GET,    "/api/complaints/public/**").permitAll()
                .antMatchers(HttpMethod.GET,    "/api/complaint-categories/public").permitAll()
                .antMatchers(HttpMethod.POST,   "/api/complaints").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/evidence").hasAnyRole("RESIDENT", "OFFICER", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/vote").hasRole("RESIDENT")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/resolution/accept").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/resolution/reject").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.GET,    "/api/complaints/my").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/update").hasAnyRole("OFFICER", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/resolve").hasAnyRole("OFFICER", "ADMIN")
                .antMatchers("/api/complaints/admin/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/verify").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/reject").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/complaints/*/assign").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/auth/otp/**").hasAnyRole("RESIDENT", "ADMIN")

                // ── Admin-only ──
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/users/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT,    "/api/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/api/contractors", "/api/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT,    "/api/contractors", "/api/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH,  "/api/contractors", "/api/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/contractors", "/api/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/projects/**").hasRole("ADMIN")

                // ── Officer ──
                .antMatchers(HttpMethod.POST, "/api/projects/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.PUT,  "/api/projects/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.POST, "/api/expenditures", "/api/expenditures/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.POST, "/api/documents", "/api/documents/**").hasAnyRole("ADMIN", "OFFICER")

                // ── Auditor + Admin ──
                .antMatchers("/api/audit-logs/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers("/api/reports/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers(HttpMethod.GET, "/api/expenditures", "/api/expenditures/**").hasAnyRole("ADMIN", "OFFICER", "AUDITOR")
                .antMatchers(HttpMethod.PUT, "/api/expenditures/**").hasAnyRole("ADMIN", "AUDITOR")

                // Anything else under /api requires authentication by default.
                .antMatchers("/api/**").authenticated()

                // Any non-API route is the SPA (e.g. /dashboard) and must be public.
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // If allowedOrigins is empty/blank, allow all origins (safe for same-origin deployment)
        if (allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
            config.setAllowedOriginPatterns(java.util.Arrays.asList("*"));
        } else {
            config.setAllowedOrigins(java.util.Arrays.asList(allowedOrigins.split(",")));
        }

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Refresh-Token"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}