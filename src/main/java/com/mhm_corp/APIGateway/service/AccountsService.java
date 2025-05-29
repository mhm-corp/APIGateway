package com.mhm_corp.APIGateway.service;

import com.mhm_corp.APIGateway.controller.dto.account.AccountInformationByNumber;
import com.mhm_corp.APIGateway.controller.dto.account.AccountInputInformation;
import com.mhm_corp.APIGateway.service.fallback.FallBackAccountsService;
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

    private final FallBackAccountsService fallBackAccountService;
    private final RestTemplate restTemplate;

    public AccountsService(FallBackAccountsService fallBackAccountService, RestTemplate restTemplate) {
        this.fallBackAccountService = fallBackAccountService;
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "cb_accountRegistration", fallbackMethod = "accountRegistrationFallback")
    public ResponseEntity<String> accountRegistration(AccountInputInformation accountInputInformation, String endpoint) {
        return executeRequest(accountInputInformation, endpoint, String.class, HttpMethod.POST, accountServiceUrl, restTemplate);
    }

    private ResponseEntity<String> accountRegistrationFallback(AccountInputInformation accountInputInformation, String endpoint, Exception e) {
        return fallBackAccountService.accountRegistration(accountInputInformation,endpoint,e);
    }

    @CircuitBreaker(name = "cb_getAccountByAccountNumber", fallbackMethod = "getAccountByAccountNumberFallback")
    public ResponseEntity<AccountInformationByNumber> getAccountByAccountNumber(String accountNumber, String endpoint) {
        String url = accountServiceUrl + endpoint + "/"+ accountNumber;
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                AccountInformationByNumber.class
        );
    }

    private ResponseEntity<String> getAccountByAccountNumberFallback(String accountNumber, String endpoint, Exception e) {
        return fallBackAccountService.getAccountByAccountNumber(accountNumber,endpoint,e);
    }
}
