package com.lms.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback controller for Circuit Breaker.
 * Returns friendly error responses when downstream services are unavailable.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/loans")
    public ResponseEntity<Map<String, Object>> loanServiceFallback() {
        return createFallbackResponse("loan-service", "Loan Service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/emi")
    public ResponseEntity<Map<String, Object>> emiServiceFallback() {
        return createFallbackResponse("emi-service", "EMI Service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/payments")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return createFallbackResponse("payment-service", "Payment Service is temporarily unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String service, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("service", service);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("suggestion", "The circuit breaker is open. Service will be retried automatically.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
