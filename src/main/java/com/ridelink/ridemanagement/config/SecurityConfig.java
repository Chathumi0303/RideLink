package com.ridelink.ridemanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ridelink.ridemanagement.dto.response.ErrorResponse;
import com.ridelink.ridemanagement.security.JwtRoleConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtRoleConverter jwtRoleConverter;
    private final String jwtSecretKey;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            JwtRoleConverter jwtRoleConverter,
            @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String jwtSecretKey) {
        this.jwtRoleConverter = jwtRoleConverter;
        this.jwtSecretKey = jwtSecretKey;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Swagger & OpenAPI endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Role-based restrictions on Ride Management endpoints
                        .requestMatchers(HttpMethod.POST, "/api/rides").hasAnyRole("PASSENGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rides/*/assign").hasAnyRole("PASSENGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rides/*/accept").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rides/*/start").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rides/*/complete").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/rides/*/cancel").authenticated()

                        // Retrieval endpoints require authentication; granular ownership validated in service layer
                        .requestMatchers("/api/rides/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                .decoder(jwtDecoder())
                        )
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtRoleConverter);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Enforce at least 256 bits (32 bytes) for HMAC-SHA256
        byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec).build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse(
                    Instant.now(),
                    401,
                    "UNAUTHORIZED",
                    "Full authentication is required to access this resource: " + authException.getMessage(),
                    request.getRequestURI()
            );
            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse(
                    Instant.now(),
                    403,
                    "FORBIDDEN",
                    "Access is denied: You do not possess the required role or authority to perform this operation",
                    request.getRequestURI()
            );
            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }
}
