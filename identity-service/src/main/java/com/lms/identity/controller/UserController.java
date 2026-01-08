package com.lms.identity.controller;

import com.lms.identity.dto.UserRegisterRequest;
import com.lms.identity.dto.UserResponse;
import com.lms.identity.service.CreditScoreService;
import com.lms.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CreditScoreService creditScoreService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getCurrentUserProfile(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/officers")
    public ResponseEntity<java.util.List<UserResponse>> getOfficers() {
        return ResponseEntity.ok(userService.getOfficers());
    }

    // Credit Score endpoints
    @GetMapping("/{id}/credit-score")
    public ResponseEntity<Map<String, Integer>> getCreditScore(@PathVariable Long id) {
        Integer score = creditScoreService.getCreditScore(id);
        return ResponseEntity.ok(Map.of("creditScore", score));
    }

    @PostMapping("/{id}/credit-score/increment")
    public ResponseEntity<Map<String, Integer>> incrementCreditScore(@PathVariable Long id) {
        Integer newScore = creditScoreService.incrementCreditScore(id);
        return ResponseEntity.ok(Map.of("creditScore", newScore));
    }

    @PutMapping("/{id}/credit-score")
    public ResponseEntity<Map<String, Integer>> setCreditScore(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer score = request.get("creditScore");
        Integer newScore = creditScoreService.setCreditScore(id, score);
        return ResponseEntity.ok(Map.of("creditScore", newScore));
    }
}
