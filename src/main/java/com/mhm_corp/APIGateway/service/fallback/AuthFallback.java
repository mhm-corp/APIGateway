package com.mhm_corp.APIGateway.service.fallback;

import com.mhm_corp.APIGateway.controller.dto.auth.LoginRequest;
import com.mhm_corp.APIGateway.controller.dto.auth.UserInformation;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class AuthFallback extends CommonFallback{

    public ResponseEntity<String> userRegistration(UserInformation userInformation, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }

    public ResponseEntity<String> loginUser(LoginRequest loginRequest, HttpServletResponse response, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }


    public ResponseEntity<String> getUserInformation(String username, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }

    public ResponseEntity<String> refreshTokenResponse(String accessToken, String refreshToken, HttpServletResponse response, String endpoint, Exception e) {
        return handleException(e, endpoint);
    }
}
