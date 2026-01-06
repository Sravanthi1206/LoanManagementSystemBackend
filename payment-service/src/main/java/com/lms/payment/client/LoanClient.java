package com.lms.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@FeignClient(name = "loan-service", fallback = LoanClientFallback.class)
public interface LoanClient {

    @PostMapping("/wallet/debit")
    void debitWallet(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/wallet/credit")
    void creditWallet(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount, @RequestParam("description") String description);
}
