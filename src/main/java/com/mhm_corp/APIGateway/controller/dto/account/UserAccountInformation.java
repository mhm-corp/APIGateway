package com.mhm_corp.APIGateway.controller.dto.account;

import com.mhm_corp.APIGateway.controller.enums.account.AccountType;
import jakarta.validation.constraints.NotNull;

public record UserAccountInformation(
        @NotNull AccountType accountType
       // @NotNull MultipartFile proofIncome
){
}
