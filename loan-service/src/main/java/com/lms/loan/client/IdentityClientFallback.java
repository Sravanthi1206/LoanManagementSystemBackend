package com.lms.loan.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class IdentityClientFallback implements FallbackFactory<IdentityClient> {

    @Override
    public IdentityClient create(Throwable cause) {
        return new IdentityClient() {
            @Override
            public Map<String, Integer> getCreditScore(Long userId) {
                log.warn("Identity service unavailable, cannot fetch credit score for user {}: {}",
                        userId, cause.getMessage());
                // Return -1 to indicate failure - caller should handle gracefully
                return Map.of("creditScore", -1);
            }
        };
    }
}
