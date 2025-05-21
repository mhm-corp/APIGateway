package com.mhm_corp.APIGateway.service;

import com.mhm_corp.APIGateway.controller.dto.account.UserAccountInformation;
import com.mhm_corp.APIGateway.service.fallback.FallBackAccountsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {
    private static final Logger logger = LoggerFactory.getLogger(AccountsService.class);

    @Value("${account.service.url}")
    private String accountServiceUrl;

    private final FallBackAccountsService fallBackAccountService;

    public AccountsService(FallBackAccountsService fallBackAccountService) {
        this.fallBackAccountService = fallBackAccountService;
    }


    public ResponseEntity<String> accountRegistration(UserAccountInformation userAccountInformation) {
        return ResponseEntity.ok("Account Registration since Service");
    }

}
