package com.mhm_corp.APIGateway.controller;


import com.mhm_corp.APIGateway.controller.dto.auth.LoginRequest;
import com.mhm_corp.APIGateway.controller.dto.auth.UserData;
import com.mhm_corp.APIGateway.controller.dto.auth.UserInformation;
import com.mhm_corp.APIGateway.service.ApiGatewayAuthService;
import com.mhm_corp.APIGateway.service.ValidateAutTokenService;
import com.mhm_corp.APIGateway.service.external.KeycloakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gateway")

@Tag(name = "The API Gateway", description = "REST API allow access to other services")
public class ApiGatewayController {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayController.class);

    private final ApiGatewayAuthService apiGatewayAuthService;
    private final KeycloakService keycloakService;
    private final ValidateAutTokenService validateAutTokenService;

    public ApiGatewayController(ApiGatewayAuthService apiGatewayAuthService, KeycloakService keycloakService, ValidateAutTokenService validateAutTokenService) {
        this.apiGatewayAuthService = apiGatewayAuthService;
        this.keycloakService = keycloakService;
        this.validateAutTokenService = validateAutTokenService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> userRegistration(@RequestBody UserInformation userInformation) {
        logger.info("Starting user registration process for username: {}", userInformation.username());
        ResponseEntity<String> response =  apiGatewayAuthService.userRegistration(userInformation, "/register");
        logger.info("User registration completed with status: {}", response.getStatusCode());
        return response;
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> loginUser (@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        logger.info("Processing login request for username: {}", loginRequest.username());
        ResponseEntity<Void> loginResponse = apiGatewayAuthService.loginUser(loginRequest, response, "/login");
        logger.info("Login attempt completed with status: {}", loginResponse.getStatusCode());
        return loginResponse;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the logged-in user's information by username or email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or token expired"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserData> getUserInformation(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        logger.info("Processing get user information request");
        boolean isValidToken = validateAutTokenService.validateAuthenticationWithToken(accessToken, refreshToken, response);
        if (!isValidToken) {
            logger.warn("Invalid or expired token detected");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Retrieving information for user: {}", username);
        ResponseEntity<UserData> userResponse = apiGatewayAuthService.getUserInformation(username, "/me");
        logger.info("User information request completed with status: {}", userResponse.getStatusCode());
        return userResponse;
    }

    @PostMapping("/refresh")
    @Operation(summary = "Use the refresh token when the token has expired")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> refreshTokenResponse(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        logger.info("Processing token refresh request");
        if (refreshToken == null || accessToken == null) {
            logger.warn("Missing refresh token or access token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseEntity<Void> refreshResponse = apiGatewayAuthService.refreshTokenResponse(accessToken, refreshToken, response, "/refresh");
        logger.info("Token refresh completed with status: {}", refreshResponse.getStatusCode());
        return refreshResponse;

    }
}
