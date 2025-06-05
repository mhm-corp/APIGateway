package com.mhm_corp.APIGateway.service;

import com.mhm_corp.APIGateway.controller.dto.account.AccountInformationByNumber;
import com.mhm_corp.APIGateway.controller.dto.account.AccountInputInformation;
import com.mhm_corp.APIGateway.service.fallback.AccountsFallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AccountsService extends CommonService {
    private static final Logger logger = LoggerFactory.getLogger(AccountsService.class);

    @Value("${account.service.url}")
    private String accountServiceUrl;

    private final AccountsFallback fallBackAccountService;
    private final RestTemplate restTemplate;

    public AccountsService(AccountsFallback fallBackAccountService, RestTemplate restTemplate) {
        this.fallBackAccountService = fallBackAccountService;
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "cb_accountRegistration", fallbackMethod = "accountRegistrationFallback")
    public ResponseEntity<String> accountRegistration(AccountInputInformation accountInputInformation, String endpoint) {
        logger.debug("Starting account registration process at endpoint: {}", endpoint);
        try {
            ResponseEntity<String> response = executeRequest(accountInputInformation, endpoint, String.class, HttpMethod.POST, accountServiceUrl, restTemplate);
            logger.info("Account registration completed with status: {}", response.getStatusCode());
            return response;
        } catch (Exception e) {
            logger.error("Account registration failed: {}", e.getMessage());
            throw e;
        }
    }

    private ResponseEntity<String> accountRegistrationFallback(AccountInputInformation accountInputInformation, String endpoint, Exception e) {
        return fallBackAccountService.accountRegistration(accountInputInformation,endpoint,e);
    }

    @CircuitBreaker(name = "cb_getAccountByAccountNumber", fallbackMethod = "getAccountByAccountNumberFallback")
    public ResponseEntity<AccountInformationByNumber> getAccountByAccountNumber(String accountNumber, String endpoint) {
        logger.debug("Fetching account information for account number: {} at endpoint: {}", accountNumber, endpoint);
        try {
            String url = accountServiceUrl + endpoint + "/" + accountNumber;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());

            ResponseEntity<AccountInformationByNumber> response =  restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    AccountInformationByNumber.class
            );
            logger.info("Successfully retrieved account information for account: {}", accountNumber);
            return response;
        }catch (Exception e) {
            logger.error("Error retrieving account information: {}", e.getMessage());
            throw e;
        }
    }

    private ResponseEntity<String> getAccountByAccountNumberFallback(String accountNumber, String endpoint, Exception e) {
        return fallBackAccountService.getAccountByAccountNumber(accountNumber,endpoint,e);
    }
}
