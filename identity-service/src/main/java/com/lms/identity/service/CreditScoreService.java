package com.lms.identity.service;

import com.lms.identity.entity.User;
import com.lms.identity.exception.UserNotFoundException;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private static final int MIN_SCORE = 300;
    private static final int MAX_SCORE = 900;
    private static final int DEFAULT_SCORE = 650;
    private static final int BASE_INCREMENT = 15; // Base points for payment

    private final UserRepository repository;

    /**
     * Gets the current credit score for a user.
     */
    public Integer getCreditScore(Long userId) {
        User user = findUser(userId);
        return user.getCreditScore() != null ? user.getCreditScore() : DEFAULT_SCORE;
    }

    /**
     * Increments the user's credit score after a successful payment.
     * Uses diminishing returns formula - higher scores get smaller increments.
     * 
     * Formula: increment = BASE_INCREMENT * (MAX_SCORE - currentScore) / (MAX_SCORE - MIN_SCORE)
     */
    @Transactional
    public Integer incrementCreditScore(Long userId) {
        User user = findUser(userId);
        int currentScore = user.getCreditScore() != null ? user.getCreditScore() : DEFAULT_SCORE;
        
        // Already at max
        if (currentScore >= MAX_SCORE) {
            log.info("User {} already at max credit score {}", userId, MAX_SCORE);
            return MAX_SCORE;
        }
        
        // Calculate increment with diminishing returns
        double ratio = (double) (MAX_SCORE - currentScore) / (MAX_SCORE - MIN_SCORE);
        int increment = (int) Math.round(BASE_INCREMENT * ratio);
        
        // Ensure minimum increment of 1 if not at max
        if (increment < 1) {
            increment = 1;
        }
        
        int newScore = Math.min(currentScore + increment, MAX_SCORE);
        
        user.setCreditScore(newScore);
        user.setCreditScoreUpdatedAt(LocalDateTime.now());
        repository.save(user);
        
        log.info("User {} credit score updated: {} -> {} (+{})", userId, currentScore, newScore, increment);
        return newScore;
    }

    /**
     * Sets a specific credit score for a user (for admin/officer use).
     */
    @Transactional
    public Integer setCreditScore(Long userId, Integer score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("Credit score must be between " + MIN_SCORE + " and " + MAX_SCORE);
        }
        
        User user = findUser(userId);
        user.setCreditScore(score);
        user.setCreditScoreUpdatedAt(LocalDateTime.now());
        repository.save(user);
        
        log.info("User {} credit score set to {}", userId, score);
        return score;
    }

    private User findUser(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
