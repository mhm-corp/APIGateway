package com.mhm_corp.APIGateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class FallBackService {
    private static final Logger logger = LoggerFactory.getLogger(FallBackService.class);

    private static final String SERVICE_UNAVAILABLE_MESSAGE = "The %s service is currently unavailable. Please try again later.";

    private ResponseEntity<String> handleException(Exception e, String operation) {
        if (e instanceof ResourceAccessException) {
            return createServiceUnavailableResponse(e, operation);
        }

        if (e instanceof HttpServerErrorException serverError) {
            if (serverError.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                String message = serverError.getResponseBodyAsString();
                return message.contains("401 UNAUTHORIZED")
                        ? ResponseEntity.status(serverError.getStatusCode()).body("Invalid user credentials")
                        : ResponseEntity.status(serverError.getStatusCode()).body(message);
            }
            return ResponseEntity.status(serverError.getStatusCode()).body(serverError.getResponseBodyAsString());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    private ResponseEntity<String> createServiceUnavailableResponse(Exception e, String operation) {
        logger.error("Error calling {}: {}", operation, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(String.format(SERVICE_UNAVAILABLE_MESSAGE, operation));
    }


    public ResponseEntity<String> registerUser(Exception e) {
        return handleException(e, "user registration");
    }

    public ResponseEntity<String> loginUser(Exception e) {
        return handleException(e, "login");
    }




}
