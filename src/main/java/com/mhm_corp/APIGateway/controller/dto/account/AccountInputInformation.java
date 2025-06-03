package com.mhm_corp.APIGateway.controller.dto.account;

import com.mhm_corp.APIGateway.controller.enums.account.AccountType;
import jakarta.validation.constraints.NotNull;

public record AccountInputInformation(
        @NotNull(message = "Account number cannot be empty")
        String accountNumber,
        @NotNull (message = "Account type cannot be empty")
        AccountType accountType
        //  @NotNull MultipartFile proofIncome

){
}
