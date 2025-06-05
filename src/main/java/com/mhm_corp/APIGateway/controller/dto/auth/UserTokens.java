package com.mhm_corp.APIGateway.controller.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTokens {
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
}