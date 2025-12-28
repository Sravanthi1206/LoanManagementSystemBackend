package com.lms.emi.service;

import com.lms.emi.dto.EmiCalculationResponse;
import com.lms.emi.entity.RepaymentSchedule;
import com.lms.emi.repository.RepaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmiService {

    private final RepaymentRepository repository;

    /**
     * Calculate EMI preview without saving
     * EMI Formula: P * r * (1+r)^n / ((1+r)^n - 1)
     */
    public EmiCalculationResponse calculateEmi(BigDecimal principal, BigDecimal annualRate, Integer tenureMonths) {
        MathContext mc = MathContext.DECIMAL128;
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), mc);
        
        BigDecimal onePlusR = monthlyRate.add(BigDecimal.ONE);
        BigDecimal powerFactor = onePlusR.pow(tenureMonths, mc);
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(powerFactor);
        BigDecimal denominator = powerFactor.subtract(BigDecimal.ONE);
        
        BigDecimal monthlyEmi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        BigDecimal totalPayment = monthlyEmi.multiply(BigDecimal.valueOf(tenureMonths));
        BigDecimal totalInterest = totalPayment.subtract(principal);
        
        return EmiCalculationResponse.builder()
                .principalAmount(principal)
                .annualInterestRate(annualRate)
                .tenureMonths(tenureMonths)
                .monthlyEmi(monthlyEmi)
                .totalInterest(totalInterest.setScale(2, RoundingMode.HALF_UP))
                .totalPayment(totalPayment.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * Generate and save EMI schedule when loan is approved
     */
    @Transactional
    public void generateSchedule(Long loanId, BigDecimal amount, BigDecimal annualInterestRate, Integer tenureMonths) {
        MathContext mc = MathContext.DECIMAL128;
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(1200), mc);
        
        BigDecimal onePlusR = monthlyRate.add(BigDecimal.ONE);
        BigDecimal powerFactor = onePlusR.pow(tenureMonths, mc);
        
        BigDecimal numerator = amount.multiply(monthlyRate).multiply(powerFactor);
        BigDecimal denominator = powerFactor.subtract(BigDecimal.ONE);
        
        BigDecimal emi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);

        BigDecimal balance = amount;
        LocalDate nextDueDate = LocalDate.now().plusMonths(1);

        List<RepaymentSchedule> schedules = new ArrayList<>();

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interestPart = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = emi.subtract(interestPart);
            
            balance = balance.subtract(principalPart);

            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .loanId(loanId)
                    .installmentNo(i)
                    .dueDate(nextDueDate)
                    .principalAmount(principalPart)
                    .interestAmount(interestPart)
                    .totalEmi(emi)
                    .status(RepaymentSchedule.PaymentStatus.PENDING)
                    .build();
            
            schedules.add(schedule);
            nextDueDate = nextDueDate.plusMonths(1);
        }
        
        repository.saveAll(schedules);
    }

    public List<RepaymentSchedule> getSchedule(Long loanId) {
        return repository.findByLoanId(loanId);
    }

    public List<RepaymentSchedule> getUpcomingEmis(Long userId) {
        // In a real app, we'd query by userId through loan service
        // For now, return pending EMIs with due date in next 30 days
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        
        return repository.findByStatusAndDueDateBetween(
                RepaymentSchedule.PaymentStatus.PENDING,
                today,
                thirtyDaysLater
        );
    }

    @Transactional
    public RepaymentSchedule markInstallmentAsPaid(Long installmentId) {
        RepaymentSchedule schedule = repository.findById(installmentId)
                .orElseThrow(() -> new RuntimeException("Installment not found with id: " + installmentId));
        
        schedule.setStatus(RepaymentSchedule.PaymentStatus.PAID);
        schedule.setPaidDate(LocalDate.now());
        
        return repository.save(schedule);
    }
}
