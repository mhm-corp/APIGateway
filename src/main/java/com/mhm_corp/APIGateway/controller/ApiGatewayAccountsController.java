package com.mhm_corp.APIGateway.controller;

import com.mhm_corp.APIGateway.controller.dto.account.AccountInformationByNumber;
import com.mhm_corp.APIGateway.controller.dto.account.AccountInputInformation;
import com.mhm_corp.APIGateway.service.AccountsService;
import com.mhm_corp.APIGateway.service.ValidateAutTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/gateway/accounts")

@Tag(name = "The API Gateway Accounts", description = "REST API allow access to accounts")
public class ApiGatewayAccountsController {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayAccountsController.class);
    private final AccountsService accountsService;
    private final ValidateAutTokenService validateAutTokenService;

    public ApiGatewayAccountsController(AccountsService accountsService, ValidateAutTokenService validateAutTokenService) {
        this.accountsService = accountsService;
        this.validateAutTokenService = validateAutTokenService;
    }


    @PostMapping
    @Operation(summary = "Register a user account")
    public ResponseEntity<String> accountRegistration (
            @RequestBody AccountInputInformation accountInputInformation,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response){
        boolean isValidAuth = validateAutTokenService.validateAuthenticationWithToken(accessToken, refreshToken, response);
        return (!isValidAuth)
                ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                : accountsService.accountRegistration(accountInputInformation,"");
    }


    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details by account number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account details retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid account number format"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountInformationByNumber> getAccountByAccountNumber(
            @PathVariable String accountNumber,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        logger.info("Received request to get account details for account number: {}", accountNumber);
        logger.debug("Validating authentication tokens");

        boolean isValidAuth = validateAutTokenService.validateAuthenticationWithToken(accessToken, refreshToken, response);
        if (!isValidAuth) {
            logger.warn("Authentication failed for account number: {}", accountNumber);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.debug("Authentication successful, proceeding to fetch account details");
        ResponseEntity<AccountInformationByNumber> result = accountsService.getAccountByAccountNumber(accountNumber, "");

        logger.info("Completed processing request for account number: {} with status: {}",
                accountNumber, result.getStatusCode());

        return result;
    }

}
