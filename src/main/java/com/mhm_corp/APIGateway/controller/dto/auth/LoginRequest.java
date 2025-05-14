package com.mhm_corp.APIGateway.controller.dto.auth;

public record LoginRequest(
        String username,
        String password
) {
}
