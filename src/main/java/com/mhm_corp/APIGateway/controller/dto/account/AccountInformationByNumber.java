package com.mhm_corp.APIGateway.controller.dto.account;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountInformationByNumber(
        String accountNumber,
        String accountType,
        LocalDate openingDate,
        String status,
        BigDecimal balance

        ) {
}
