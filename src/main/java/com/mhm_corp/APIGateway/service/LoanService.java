package com.mhm_corp.APIGateway.service;

import com.mhm_corp.APIGateway.controller.dto.loan.InputDataLoan;
import com.mhm_corp.APIGateway.service.fallback.LoanFallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoanService  extends CommonService {
    private static final Logger logger = LoggerFactory.getLogger(AccountsService.class);
    private final RestTemplate restTemplate;
    private final LoanFallback loanFallback;

    @Value("${loan.service.url}")
    private String loanServiceUrl;

    public LoanService(RestTemplate restTemplate, LoanFallback loanFallback) {
        this.restTemplate = restTemplate;
        this.loanFallback = loanFallback;
    }

    @CircuitBreaker(name = "cb_loanRegistration", fallbackMethod = "loanRegistrationFallback")
    public ResponseEntity<String> loanRegistration(InputDataLoan inputDataLoan, String endpoint) {
        logger.info("Starting loan registration process for account {}", inputDataLoan.accountNumber());
        try {
            ResponseEntity<String> response = executeRequest(inputDataLoan, endpoint, String.class, HttpMethod.POST, loanServiceUrl, restTemplate);
            logger.info("Account registration completed with status: {}", response.getStatusCode());
            return response;
        } catch (Exception e) {
            logger.error("Account registration failed: {}", e.getMessage());
            throw e;
        }

    }

    private ResponseEntity<String> loanRegistrationFallback(InputDataLoan inputDataLoan, String endpoint, Exception e) {
        return loanFallback.loanRegistration(inputDataLoan,endpoint,e);
    }

    @CircuitBreaker(name = "cb_payLoan", fallbackMethod = "payLoanFallback")
    public ResponseEntity<String> payLoan(Long id, String endpoint) {
        logger.info("Processing loan payment for ID: {}", id);
        try {
            String url = loanServiceUrl + endpoint;
            logger.debug("Making request to URL: {}", url);
            ResponseEntity<String> response = executeRequest(id, "", String.class, HttpMethod.POST, url, restTemplate);
            logger.info("Loan payment completed for ID: {} with status: {}", id, response.getStatusCode());
            return response;
        } catch (Exception e) {
            logger.error("Error processing loan payment for ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    private ResponseEntity<String> payLoanFallback(Long id, String endpoint, Exception e) {
        return loanFallback.payLoanFallback(id,endpoint,e);
    }

}
