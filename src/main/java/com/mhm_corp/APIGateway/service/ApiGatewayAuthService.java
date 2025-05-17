package com.mhm_corp.APIGateway.service;


import com.mhm_corp.APIGateway.controller.dto.auth.LoginRequest;
import com.mhm_corp.APIGateway.controller.dto.auth.UserData;
import com.mhm_corp.APIGateway.controller.dto.auth.UserInformation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApiGatewayAuthService {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayAuthService.class);
    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;
    private final FallBackAuthService fallBackAuthService;

    public ApiGatewayAuthService(RestTemplate restTemplate, FallBackAuthService fallBackAuthService) {
        this.restTemplate = restTemplate;
        this.fallBackAuthService = fallBackAuthService;
    }

    private <T, R> ResponseEntity<R> executeRequest(T request, String endpoint, Class<R> responseType, HttpMethod method) {
        String url = authServiceUrl + endpoint;
        HttpEntity<T> requestEntity = new HttpEntity<>(request, createHeaders());

        ResponseEntity<R> response = restTemplate.exchange(
                url,
                method,
                requestEntity,
                responseType
        );

        return (ResponseEntity<R>) createResponseEntity(response);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private ResponseEntity<?> createResponseEntity(ResponseEntity<?> response) {
        return ResponseEntity
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    @CircuitBreaker(name = "cb_userRegistration", fallbackMethod = "userRegistrationFallback")
    public ResponseEntity<String> userRegistration(UserInformation userInformation, String endpoint) {
        return executeRequest(userInformation, endpoint, String.class, HttpMethod.POST);
    }

    private ResponseEntity<String> userRegistrationFallback(UserInformation userInformation, String endpoint, Exception e) {
        return fallBackAuthService.userRegistration(userInformation,endpoint,e);
    }

    @CircuitBreaker(name = "cb_loginUser", fallbackMethod = "loginUserFallback")
    public ResponseEntity<Void> loginUser(LoginRequest loginRequest, HttpServletResponse response, String endpoint) {
        ResponseEntity<Void> authResponse = executeRequest(loginRequest, endpoint, Void.class, HttpMethod.POST);

        HttpHeaders headers = authResponse.getHeaders();
        headers.getOrEmpty(HttpHeaders.SET_COOKIE)
                .forEach(cookie -> response.addHeader(HttpHeaders.SET_COOKIE, cookie));

        return authResponse;
    }

    private ResponseEntity<String> loginUserFallback(LoginRequest loginRequest, HttpServletResponse response, String endpoint, Exception e) {
        return fallBackAuthService.loginUser(loginRequest,response,endpoint,e);
    }

    @CircuitBreaker(name = "cb_getUserInformation", fallbackMethod = "getUserInformationFallback")
    public ResponseEntity<UserData> getUserInformation(String username,String endpoint) {
        String url = authServiceUrl + endpoint + "?username=" + username;
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                UserData.class
        );
    }

    private ResponseEntity<String> getUserInformationFallback(String username, String endpoint, Exception e) {
        return fallBackAuthService.getUserInformation(username, endpoint, e);
    }

    @CircuitBreaker(name = "cb_refreshTokenResponse", fallbackMethod = "refreshTokenResponseFallback")
    public ResponseEntity<Void> refreshTokenResponse(String accessToken, String refreshToken,
                                                     HttpServletResponse response, String endpoint) {
        HttpHeaders headers = createHeaders();

        Map<String, String> tokenBody = new HashMap<>();
        tokenBody.put("accessToken", accessToken);
        tokenBody.put("refreshToken", refreshToken);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(tokenBody, headers);

        ResponseEntity<Void> authResponse = restTemplate.exchange(
                authServiceUrl + endpoint,
                HttpMethod.POST,
                requestEntity,
                Void.class
        );

        HttpHeaders responseHeaders = authResponse.getHeaders();
        responseHeaders.getOrEmpty(HttpHeaders.SET_COOKIE)
                .forEach(cookie -> response.addHeader(HttpHeaders.SET_COOKIE, cookie));

        return authResponse;
    }

    private ResponseEntity<String> refreshTokenResponseFallback(
            String accessToken, String refreshToken,
            HttpServletResponse response, String endpoint, Exception e) {
        return fallBackAuthService.refreshTokenResponse(accessToken, refreshToken, response, endpoint, e);
    }
}
