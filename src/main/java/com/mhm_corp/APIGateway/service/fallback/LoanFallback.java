package com.mhm_corp.APIGateway.service.fallback;

import com.mhm_corp.APIGateway.controller.dto.loan.InputDataLoan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LoanFallback extends CommonFallback {
    private static final Logger logger = LoggerFactory.getLogger(LoanFallback.class);

    public ResponseEntity<String> loanRegistration(InputDataLoan inputDataLoan, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }
}
