package com.mhm_corp.APIGateway.service;

import com.mhm_corp.APIGateway.service.external.KeycloakService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AutTokenValidationService {
    private static final Logger logger = LoggerFactory.getLogger(AutTokenValidationService.class);
    private final AuthService authService;
    private final KeycloakService keycloakService;

    public AutTokenValidationService(AuthService authService, KeycloakService keycloakService) {
        this.authService = authService;
        this.keycloakService = keycloakService;
    }

    public boolean validateAuthenticationWithToken(
            String accessToken, String refreshToken, HttpServletResponse response) {
        logger.debug("Starting token validation process");
        if (accessToken == null || refreshToken == null) {
            logger.warn("Missing access token or refresh token");
            return false;
        }

        if (keycloakService.validateToken(accessToken)) {
            logger.info("Access token is valid");
            return true;
        }

        logger.info("Access token expired, attempting token refresh");
        ResponseEntity<Void> refreshResponse = authService.refreshTokenResponse(
                accessToken, refreshToken, response, "/refresh");
        boolean isValid = refreshResponse.getStatusCode() == HttpStatus.OK;
        logger.info("Token refresh completed. Success: {}", isValid);
        return isValid;
    }
}
