package com.lms.loan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "identity-service", fallbackFactory = IdentityClientFallback.class)
public interface IdentityClient {

    @GetMapping("/users/{userId}/credit-score")
    Map<String, Integer> getCreditScore(@PathVariable("userId") Long userId);
}
