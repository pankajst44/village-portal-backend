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
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.cors.allowed-origins}")
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
                // ── Public endpoints — no authentication required ──
                .antMatchers("/auth/login", "/auth/refresh").permitAll()

                // ── Public read-only portal ──
                .antMatchers(HttpMethod.GET, "/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/projects/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/funds/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/contractors/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/documents/public/**").permitAll()

                // ── Admin-only endpoints ──
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/users/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT,  "/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/contractors/**").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/projects/**").hasRole("ADMIN")

                // ── Officer endpoints ──
                .antMatchers(HttpMethod.POST, "/projects/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.PUT,  "/projects/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.POST, "/expenditures/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.POST, "/documents/**").hasAnyRole("ADMIN", "OFFICER")

                // ── Auditor + Admin: reports and audit logs ──
                .antMatchers("/audit-logs/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers("/reports/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers(HttpMethod.GET, "/expenditures/**").hasAnyRole("ADMIN", "OFFICER", "AUDITOR")
                .antMatchers(HttpMethod.PUT, "/expenditures/*/verify").hasAnyRole("ADMIN", "AUDITOR")

                // ── All other requests require authentication ──
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList(allowedOrigins));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
