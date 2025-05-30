package com.mhm_corp.APIGateway.service.fallback;

import com.mhm_corp.APIGateway.controller.dto.account.AccountInputInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FallBackAccountsService {
    private static final Logger logger = LoggerFactory.getLogger(FallBackAccountsService.class);

    private static final String SERVICE_UNAVAILABLE_MESSAGE = "The %s service is currently unavailable. Please try again later.";

    private ResponseEntity<String> handleException(Exception e, String operation) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }


    private ResponseEntity<String> createServiceUnavailableResponse(Exception e, String operation) {
        logger.error("Error calling {}: {}", operation, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(String.format(SERVICE_UNAVAILABLE_MESSAGE, operation));
    }
    public ResponseEntity<String> accountRegistration(AccountInputInformation accountInputInformation, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }

    public ResponseEntity<String> getAccountByAccountNumber(String accountNumber, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }
}
