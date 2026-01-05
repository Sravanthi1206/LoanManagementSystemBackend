package com.lms.emi.service;

import com.lms.emi.dto.EmiCalculationResponse;
import com.lms.emi.entity.RepaymentSchedule;
import com.lms.emi.repository.RepaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmiService Tests")
class EmiServiceTest {

    @Mock
    private RepaymentRepository repository;

    @InjectMocks
    private EmiService emiService;

    private BigDecimal principal;
    private BigDecimal rate;
    private Integer tenure;

    @BeforeEach
    void setUp() {
        principal = new BigDecimal("100000");
        rate = new BigDecimal("12");
        tenure = 12;
    }

    @Nested
    @DisplayName("EMI Calculation Tests")
    class EmiCalculationTests {

        @Test
        @DisplayName("Should calculate EMI correctly")
        void calculateEmi_ValidInput_ReturnsCorrectEmi() {
            EmiCalculationResponse result = emiService.calculateEmi(principal, rate, tenure);

            assertNotNull(result);
            assertNotNull(result.getMonthlyEmi());
            assertTrue(result.getMonthlyEmi().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Should calculate total interest")
        void calculateEmi_ReturnsCorrectTotalInterest() {
            EmiCalculationResponse result = emiService.calculateEmi(principal, rate, tenure);

            assertNotNull(result.getTotalInterest());
            assertTrue(result.getTotalInterest().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Should calculate total payment")
        void calculateEmi_ReturnsCorrectTotalPayment() {
            EmiCalculationResponse result = emiService.calculateEmi(principal, rate, tenure);

            assertNotNull(result.getTotalPayment());
            assertTrue(result.getTotalPayment().compareTo(principal) > 0);
        }
    }

    @Nested
    @DisplayName("EMI Schedule Generation Tests")
    class EmiScheduleGenerationTests {

        @Test
        @DisplayName("Should generate EMI schedule")
        void generateSchedule_Success() {
            when(repository.saveAll(anyList())).thenReturn(Collections.emptyList());

            emiService.generateSchedule(1L, 1L, principal, rate, tenure);

            verify(repository).saveAll(argThat(list -> ((List)list).size() == 12));
        }
    }

    @Nested
    @DisplayName("Get Schedule Tests")
    class GetScheduleTests {

        @Test
        @DisplayName("Should get schedule by loan ID")
        void getSchedule_Success() {
            List<RepaymentSchedule> schedules = Collections.singletonList(new RepaymentSchedule());
            when(repository.findByLoanIdOrderByInstallmentNoAsc(1L)).thenReturn(schedules);

            List<RepaymentSchedule> result = emiService.getSchedule(1L);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should get upcoming EMIs")
        void getUpcomingEmis_Success() {
            List<RepaymentSchedule> schedules = Collections.singletonList(new RepaymentSchedule());
            when(repository.findByUserIdAndStatusOrderByDueDateAsc(anyLong(), any())).thenReturn(schedules);

            List<RepaymentSchedule> result = emiService.getUpcomingEmis(1L);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Mark Installment Paid Tests")
    class MarkInstallmentPaidTests {

        @Test
        @DisplayName("Should mark installment as paid")
        void markInstallmentAsPaid_Success() {
            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .id(1L)
                    .status(RepaymentSchedule.PaymentStatus.PENDING)
                    .build();
            when(repository.findById(1L)).thenReturn(Optional.of(schedule));
            when(repository.save(any(RepaymentSchedule.class))).thenReturn(schedule);

            RepaymentSchedule result = emiService.markInstallmentAsPaid(1L);

            assertNotNull(result);
            assertEquals(RepaymentSchedule.PaymentStatus.PAID, result.getStatus());
            verify(repository).save(any(RepaymentSchedule.class));
        }

        @Test
        @DisplayName("Should throw exception when installment not found")
        void markInstallmentAsPaid_NotFound() {
            when(repository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> emiService.markInstallmentAsPaid(1L));
        }
    }
}
