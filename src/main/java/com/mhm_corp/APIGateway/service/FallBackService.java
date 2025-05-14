package com.mhm_corp.APIGateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class FallBackService {
    private static final Logger logger = LoggerFactory.getLogger(FallBackService.class);

    private ResponseEntity<String> handleException(Exception e, String operation) {
        if (e instanceof HttpClientErrorException clientError) {
            return ResponseEntity
                    .status(clientError.getStatusCode())
                    .body(clientError.getResponseBodyAsString());
        }

        logger.error("Error calling {}: {}", operation, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("The " + operation + " service is currently unavailable. Please try again later.");
    }

    public ResponseEntity<String> registerUser(Exception e) {
        return handleException(e, "user registration");
    }

    public ResponseEntity<String> loginUser(Exception e) {
        return handleException(e, "login");
    }




}
