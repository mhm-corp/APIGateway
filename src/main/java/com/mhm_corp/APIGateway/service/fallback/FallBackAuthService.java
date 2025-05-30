package com.mhm_corp.APIGateway.service.fallback;

import com.mhm_corp.APIGateway.controller.dto.auth.LoginRequest;
import com.mhm_corp.APIGateway.controller.dto.auth.UserInformation;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class FallBackAuthService {
    private static final Logger logger = LoggerFactory.getLogger(FallBackAuthService.class);
    private static final String CONNECTION_ERROR_KEYCLOAK = "/protocol/openid-connect/token";
    private static final String KEYCLOAK_ERROR_MESSAGE = "Keycloak authentication service is experiencing issues. Please try again later.";

    private static final String SERVICE_UNAVAILABLE_MESSAGE = "The %s service is currently unavailable. Please try again later.";

    private ResponseEntity<String> handleException(Exception e, String operation) {
        if (e instanceof ResourceAccessException) {
            return handleResourceAccessException((ResourceAccessException) e, operation);
        }
        if (e instanceof HttpClientErrorException clientError) {
            return handleClientError(clientError);
        }
        if (e instanceof HttpServerErrorException serverError) {
            return handleServerError(serverError);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    private ResponseEntity<String> handleResourceAccessException(ResourceAccessException e, String operation) {
        String message = e.getMessage();
        if (message != null && message.contains(CONNECTION_ERROR_KEYCLOAK)) {
            logger.error("Keycloak token endpoint error: {}", message);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(KEYCLOAK_ERROR_MESSAGE);
        }
        return createServiceUnavailableResponse(e, operation);
    }

    private ResponseEntity<String> handleClientError(HttpClientErrorException clientError) {
        String message = clientError.getMessage();
        if (message.contains(CONNECTION_ERROR_KEYCLOAK)) {
            return ResponseEntity
                    .status(clientError.getStatusCode())
                    .body(KEYCLOAK_ERROR_MESSAGE);
        }
        int colonIndex = message.indexOf(": ");
        if (colonIndex != -1 && colonIndex + 2 < message.length()) {
            String errorJson = message.substring(colonIndex + 2);
            return ResponseEntity.status(clientError.getStatusCode()).body(errorJson);
        }
        return ResponseEntity.status(clientError.getStatusCode()).body(clientError.getResponseBodyAsString());
    }

    private ResponseEntity<String> handleServerError(HttpServerErrorException serverError) {
        String message = serverError.getResponseBodyAsString();
        if (message.contains(CONNECTION_ERROR_KEYCLOAK)) {
            return ResponseEntity
                    .status(serverError.getStatusCode())
                    .body(KEYCLOAK_ERROR_MESSAGE);
        }
        if (serverError.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            return message.contains("401 UNAUTHORIZED")
                    ? ResponseEntity.status(serverError.getStatusCode()).body("Invalid user credentials")
                    : ResponseEntity.status(serverError.getStatusCode()).body(message);
        }
        return ResponseEntity.status(serverError.getStatusCode()).body(serverError.getResponseBodyAsString());
    }

    private ResponseEntity<String> createServiceUnavailableResponse(Exception e, String operation) {
        logger.error("Error calling {}: {}", operation, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(String.format(SERVICE_UNAVAILABLE_MESSAGE, operation));
    }


    public ResponseEntity<String> userRegistration(UserInformation userInformation, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }

    public ResponseEntity<String> loginUser(LoginRequest loginRequest, HttpServletResponse response, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }


    public ResponseEntity<String> getUserInformation(String username, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }

    public ResponseEntity<String> refreshTokenResponse(String accessToken, String refreshToken, HttpServletResponse response, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }
}
