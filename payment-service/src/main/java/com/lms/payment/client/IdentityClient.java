package com.lms.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "identity-service", fallbackFactory = IdentityClientFallback.class)
public interface IdentityClient {

    @PostMapping("/users/{userId}/credit-score/increment")
    Map<String, Integer> incrementCreditScore(@PathVariable("userId") Long userId);
}
