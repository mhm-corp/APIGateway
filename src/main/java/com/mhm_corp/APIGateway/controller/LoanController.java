package com.mhm_corp.APIGateway.controller;

import com.mhm_corp.APIGateway.controller.dto.loan.InputDataLoan;
import com.mhm_corp.APIGateway.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/gateway/loans")
@Tag(name = "Bank loans management, simulations, and installment payments API", description = "REST API related with loans")
public class LoanController {
    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }


    @PostMapping
    @Operation(summary = "Register a loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Loan already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error ")
    })
    public ResponseEntity<String> loanRegistration (@Valid @RequestBody InputDataLoan inputDataLoan) {
        logger.info("Received loan registration request for account {}", inputDataLoan.accountNumber());


        try {
            ResponseEntity<String> result = loanService.loanRegistration(inputDataLoan, "");
            logger.info("Completed processing request for loan: {} with status: {}",
                    inputDataLoan.accountNumber(), result.getStatusCode());
            return result;
        } catch (Exception e) {
            logger.error("Error during loan registration for account {}: {}",
                    inputDataLoan.accountNumber(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing loan registration");
        }
    }


    @PostMapping("/{id}/pay")
    @Operation(summary = "Pay a loan installment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> payLoan(@PathVariable Long id) {
        logger.info("Received loan payment request for loan ID: {}", id);
        try {
            String url = "/{id}/pay".replace("{id}", id.toString());
            ResponseEntity<String> result = loanService.payLoan(id,url);
            logger.info("Completed processing payment for loan ID: {} with status: {}", id, result.getStatusCode());
            return result;
        } catch (Exception e) {
            logger.error("Error processing payment for loan ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing loan registration");
        }
    }
}
