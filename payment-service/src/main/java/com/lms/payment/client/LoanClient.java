package com.lms.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@FeignClient(name = "loan-service")
public interface LoanClient {

    @PostMapping("/wallet/debit")
    void debitWallet(@RequestParam("userId") Long userId, @RequestParam("amount") BigDecimal amount);
}
