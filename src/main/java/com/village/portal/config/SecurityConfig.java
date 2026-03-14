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
                // ── CORS preflight ──
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ── Auth ──
                .antMatchers("/auth/login", "/auth/refresh","/auth/register/resident").permitAll()

                // ── Public portal — no auth required ──
                .antMatchers(HttpMethod.GET, "/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/projects/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/funds/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/contractors/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/documents/public/**").permitAll()
                // Document download: isPublic docs served to everyone
                .antMatchers(HttpMethod.GET, "/documents/*/download").permitAll()

                // ── Complaint routes (added for CMS — Phase 2) ──
                .antMatchers(HttpMethod.GET,    "/complaints/public/**").permitAll()
                .antMatchers(HttpMethod.GET,    "/complaint-categories/public").permitAll()
                .antMatchers(HttpMethod.POST,   "/complaints").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/complaints/*/evidence").hasAnyRole("RESIDENT", "OFFICER", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/complaints/*/vote").hasRole("RESIDENT")
                .antMatchers(HttpMethod.POST,   "/complaints/*/resolution/accept").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/complaints/*/resolution/reject").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.GET,    "/complaints/my").hasAnyRole("RESIDENT", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/complaints/*/update").hasAnyRole("OFFICER", "ADMIN")
                .antMatchers(HttpMethod.POST,   "/complaints/*/resolve").hasAnyRole("OFFICER", "ADMIN")
                .antMatchers("/complaints/admin/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers(HttpMethod.POST,   "/complaints/*/verify").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/complaints/*/reject").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/complaints/*/assign").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/auth/otp/**").hasAnyRole("RESIDENT", "ADMIN")

                // ── Admin-only ──
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/users/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT,    "/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/funds/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST,   "/contractors", "/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT,    "/contractors", "/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH,  "/contractors", "/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/contractors", "/contractors/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/projects/**").hasRole("ADMIN")

                // ── Officer ──
                .antMatchers(HttpMethod.POST, "/projects/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.PUT,  "/projects/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.POST, "/expenditures", "/expenditures/**").hasAnyRole("ADMIN", "OFFICER")
                .antMatchers(HttpMethod.POST, "/documents", "/documents/**").hasAnyRole("ADMIN", "OFFICER")

                // ── Auditor + Admin ──
                .antMatchers("/audit-logs/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers("/reports/**").hasAnyRole("ADMIN", "AUDITOR")
                .antMatchers(HttpMethod.GET, "/expenditures", "/expenditures/**").hasAnyRole("ADMIN", "OFFICER", "AUDITOR")
                .antMatchers(HttpMethod.PUT, "/expenditures/**").hasAnyRole("ADMIN", "AUDITOR")

                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        java.util.List<String> origins = java.util.Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Refresh-Token"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}