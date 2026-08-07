package com.example.ecommerce.config;

import com.example.ecommerce.auth.jwt.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
        "/api/status",
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/auth/verify-email",
        "/api/auth/resend-verification",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        // Razorpay calls this directly — no JWT to present. Authenticity comes from verifying
        // X-Razorpay-Signature against the webhook secret inside the handler itself, not from
        // Spring Security's filter chain.
        "/api/webhooks/razorpay",
    };

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
                // Order matters: these fixed-name rules must be checked before the general listings
                // wildcard below, since all match the same URL shape and Spring Security
                // takes the first matching rule.
                .requestMatchers(HttpMethod.GET, "/api/listings/mine", "/api/listings/purchases", "/api/listings/sales")
                .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/listings", "/api/listings/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/*", "/api/users/*/ratings").permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/payments/*/refund").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint((request, response, authException) -> {
                    // Hand-written body (rather than depending on a concrete Jackson ObjectMapper bean,
                    // whose type Spring Boot 4's Jackson-3-by-default autoconfiguration can change) —
                    // this is a fixed, tiny shape matching GlobalExceptionHandler's ProblemDetail output.
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("""
                        {"type":"about:blank","title":"Unauthorized","status":401,\
                        "detail":"Authentication required","errorCode":"UNAUTHENTICATED"}""");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Reached when a request matcher's hasAuthority(...) rejects an authenticated
                    // but under-privileged caller (e.g. a non-admin hitting /api/admin/**) — this
                    // happens in Spring Security's own filter chain, before the request would ever
                    // reach GlobalExceptionHandler, so it needs its own JSON body here.
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("""
                        {"type":"about:blank","title":"Forbidden","status":403,\
                        "detail":"You don't have permission to access this resource","errorCode":"ACCESS_DENIED"}""");
                }));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // The refresh-token cookie must travel on cross-origin XHR/fetch calls from either frontend.
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
