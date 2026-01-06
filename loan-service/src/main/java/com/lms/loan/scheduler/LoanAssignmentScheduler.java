package com.lms.loan.scheduler;

import com.lms.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to release stale assigned loans back to the pool.
 * Runs hourly and releases loans that have been assigned but not acted upon for 36 hours.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoanAssignmentScheduler {

    private static final int STALE_TIMEOUT_HOURS = 36;
    private final LoanService loanService;

    /**
     * Runs every hour to check for stale loan assignments.
     * If a loan has been UNDER_REVIEW for more than 36 hours without action,
     * it is released back to the pool (status = APPLIED, assignedOfficerId = null).
     */
    @Scheduled(fixedRate = 3600000) // Every hour (3600000 ms)
    public void releaseStaleLoans() {
        log.info("Running stale loan release check...");
        int released = loanService.releaseStaleLoans(STALE_TIMEOUT_HOURS);
        if (released > 0) {
            log.info("Released {} stale loans back to pool", released);
        } else {
            log.debug("No stale loans found");
        }
    }
}
