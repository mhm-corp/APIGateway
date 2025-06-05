package com.mhm_corp.APIGateway.service.fallback;

import com.mhm_corp.APIGateway.controller.dto.account.AccountInputInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AccountsFallback extends CommonFallback {
    private static final Logger logger = LoggerFactory.getLogger(AccountsFallback.class);


    public ResponseEntity<String> accountRegistration(AccountInputInformation accountInputInformation, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }

    public ResponseEntity<String> getAccountByAccountNumber(String accountNumber, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }
}
