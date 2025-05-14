package com.mhm_corp.APIGateway.service;


import com.mhm_corp.APIGateway.controller.dto.auth.LoginRequest;
import com.mhm_corp.APIGateway.controller.dto.auth.UserInformation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiGatewayAuthService {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayAuthService.class);
    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;
    private final FallBackService fallBackService;

    public ApiGatewayAuthService(RestTemplate restTemplate, FallBackService fallBackService) {
        this.restTemplate = restTemplate;
        this.fallBackService = fallBackService;
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

    @CircuitBreaker(name = "cb_registerUser", fallbackMethod = "registerUserFallback")
    public ResponseEntity<String> registerUser(UserInformation userInformation, String endpoint) {
        return executeRequest(userInformation, endpoint, String.class, HttpMethod.POST);
    }

    private ResponseEntity<String> registerUserFallback(Exception e) {
        return fallBackService.registerUser(e);
    }

    @CircuitBreaker(name = "cb_loginUser", fallbackMethod = "loginUserFallback")
    public ResponseEntity<Void> loginUser(LoginRequest loginRequest, HttpServletResponse response, String endpoint) {
        ResponseEntity<Void> authResponse = executeRequest(loginRequest, endpoint, Void.class, HttpMethod.POST);

        HttpHeaders headers = authResponse.getHeaders();
        if (headers != null && headers.containsKey(HttpHeaders.SET_COOKIE)) {
            headers.get(HttpHeaders.SET_COOKIE)
                    .forEach(cookie -> response.addHeader(HttpHeaders.SET_COOKIE, cookie));
        }

        return authResponse;
    }

    private ResponseEntity<String> loginUserFallback(Exception e) {
        return fallBackService.loginUser(e);
    }


}
