package com.mhm_corp.APIGateway.controller.dto.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InputDataLoan(
        @NotNull(message = "Account number cannot be empty")
        String accountNumber,
        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be greater than 0")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,
        @NotNull(message = "Installments cannot be null")
        @Min(value = 1, message = "Minimum number of installments is 1")
        int installments,
        @NotNull(message = "Interest rate cannot be null")
        @DecimalMin(value = "0.0", inclusive = true, message = "Interest rate must be positive or zero")
        BigDecimal effectiveAnnualInterestRate
) {

}
