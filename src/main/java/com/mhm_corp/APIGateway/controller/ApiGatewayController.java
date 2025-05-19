package com.mhm_corp.APIGateway.controller;


import com.mhm_corp.APIGateway.controller.dto.auth.LoginRequest;
import com.mhm_corp.APIGateway.controller.dto.auth.UserData;
import com.mhm_corp.APIGateway.controller.dto.auth.UserInformation;
import com.mhm_corp.APIGateway.service.ApiGatewayAuthService;
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

    public ApiGatewayController(ApiGatewayAuthService apiGatewayAuthService, KeycloakService keycloakService) {
        this.apiGatewayAuthService = apiGatewayAuthService;
        this.keycloakService = keycloakService;
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
        return apiGatewayAuthService.userRegistration(userInformation, "/register");
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
        return apiGatewayAuthService.loginUser(loginRequest, response, "/login");
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

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!keycloakService.validateToken(accessToken)) {
            if (refreshToken != null) {
                ResponseEntity<Void> refreshResponse = refreshTokenResponse(accessToken, refreshToken, response);
                if (refreshResponse.getStatusCode() == HttpStatus.OK) {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    String username = authentication.getName();
                    return apiGatewayAuthService.getUserInformation(username, "/me");
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return apiGatewayAuthService.getUserInformation(username, "/me");
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
        if (refreshToken == null || accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return apiGatewayAuthService.refreshTokenResponse (accessToken, refreshToken, response, "/refresh");
    }
}
