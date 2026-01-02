package com.lms.loan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "emi-service", fallback = EmiClientFallback.class)
public interface EmiClient {

    @PostMapping("/emi/generate")
    void generateSchedule(
            @RequestParam("loanId") Long loanId,
            @RequestParam("userId") Long userId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("rate") BigDecimal rate,
            @RequestParam("tenure") Integer tenure
    );
}
