package com.mhm_corp.APIGateway.config;


import com.mhm_corp.APIGateway.service.external.KeycloakService;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private JwtAuthentication jwtAuthentication;
    private KeycloakService keycloakService;

    private static final String NAME_TOKEN_IN_COOKIE = "accessToken";

    public SecurityConfig(JwtAuthentication jwtAuthentication, KeycloakService keycloakService) {
        this.jwtAuthentication = jwtAuthentication;
        this.keycloakService = keycloakService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/gateway/register").permitAll()
                        .requestMatchers("/api/gateway/login").permitAll()
                        .requestMatchers("/api/gateway/refresh").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> {
                    oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthentication));
                    oauth2.bearerTokenResolver(bearerTokenResolver());
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    BearerTokenResolver bearerTokenResolver() {
        return request -> {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                logger.debug("No cookies found");
                return null;
            }

            String token = Arrays.stream(cookies)
                    .filter(cookie -> NAME_TOKEN_IN_COOKIE.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (token != null && keycloakService.validateToken(token)) {
                return token;
            }
            logger.debug("Invalid token");
            return null;
        };
    }


}
