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
    private static final MathContext MC = MathContext.DECIMAL128;

    public EmiCalculationResponse calculateEmi(BigDecimal principal, BigDecimal annualRate, Integer tenureMonths) {
        BigDecimal monthlyEmi = computeMonthlyEmi(principal, annualRate, tenureMonths);
        BigDecimal totalPayment = monthlyEmi.multiply(BigDecimal.valueOf(tenureMonths));
        BigDecimal totalInterest = totalPayment.subtract(principal);
        
        return EmiCalculationResponse.builder()
                .principalAmount(principal)
                .annualInterestRate(annualRate)
                .tenureMonths(tenureMonths)
                .monthlyEmi(monthlyEmi)
                .totalInterest(totalInterest)
                .totalPayment(totalPayment)
                .build();
    }

    @Transactional
    public void generateSchedule(Long loanId, Long userId, BigDecimal amount, BigDecimal annualInterestRate, Integer tenureMonths) {
        BigDecimal monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(1200), MC);
        BigDecimal emi = computeMonthlyEmi(amount, annualInterestRate, tenureMonths);
        BigDecimal balance = amount;
        LocalDate dueDate = LocalDate.now().plusMonths(1);
        List<RepaymentSchedule> schedules = new ArrayList<>();

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interest = balance.multiply(monthlyRate, MC);
            BigDecimal principal = emi.subtract(interest);
            balance = balance.subtract(principal);

            schedules.add(RepaymentSchedule.builder()
                    .loanId(loanId)
                    .userId(userId)
                    .installmentNo(i)
                    .dueDate(dueDate)
                    .principalAmount(principal)
                    .interestAmount(interest)
                    .totalEmi(emi)
                    .status(RepaymentSchedule.PaymentStatus.PENDING)
                    .build());
            dueDate = dueDate.plusMonths(1);
        }
        repository.saveAll(schedules);
    }

    public List<RepaymentSchedule> getSchedule(Long loanId) {
        return repository.findByLoanIdOrderByInstallmentNoAsc(loanId);
    }

    public List<RepaymentSchedule> getUpcomingEmis(Long userId) {
        // Return all pending EMIs for the user, ordered by due date
        return repository.findByUserIdAndStatusOrderByDueDateAsc(
                userId, RepaymentSchedule.PaymentStatus.PENDING);
    }

    @Transactional
    public RepaymentSchedule markInstallmentAsPaid(Long installmentId) {
        RepaymentSchedule schedule = repository.findById(installmentId)
                .orElseThrow(() -> new com.lms.emi.exception.InstallmentNotFoundException(installmentId));
        schedule.setStatus(RepaymentSchedule.PaymentStatus.PAID);
        schedule.setPaidDate(LocalDate.now());
        return repository.save(schedule);
    }

    private BigDecimal computeMonthlyEmi(BigDecimal principal, BigDecimal annualRate, int months) {
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), MC);
        BigDecimal onePlusR = monthlyRate.add(BigDecimal.ONE);
        BigDecimal powerFactor = onePlusR.pow(months, MC);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(powerFactor);
        BigDecimal denominator = powerFactor.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, MC);
    }
}
