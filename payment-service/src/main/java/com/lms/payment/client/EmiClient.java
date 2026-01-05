package com.lms.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "emi-service", fallback = EmiClientFallback.class)
public interface EmiClient {

    @PutMapping("/emi/installment/{id}/paid")
    void markInstallmentAsPaid(@PathVariable("id") Long id);
}
