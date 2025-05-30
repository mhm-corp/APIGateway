package com.mhm_corp.APIGateway.controller.enums.account;

public enum AccountType {
    CHECKING("Checking"),
    SAVINGS("Savings"),
    BUSINESS("Business");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}