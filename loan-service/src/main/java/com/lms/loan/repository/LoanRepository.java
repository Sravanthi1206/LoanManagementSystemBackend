package com.lms.loan.repository;

import com.lms.loan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable);
    
    // Officer-specific queries
    Page<Loan> findByAssignedOfficerId(Long officerId, Pageable pageable);
    Page<Loan> findByAssignedOfficerIdAndStatus(Long officerId, Loan.LoanStatus status, Pageable pageable);
    Page<Loan> findByStatusAndAssignedOfficerIdIsNull(Loan.LoanStatus status, Pageable pageable);
    
    // Count pending reviews per officer
    long countByAssignedOfficerIdAndStatus(Long officerId, Loan.LoanStatus status);
    
    // Find stale assigned loans (for scheduler)
    @Query("SELECT l FROM Loan l WHERE l.status = :status AND l.assignedAt < :cutoffTime AND l.assignedOfficerId IS NOT NULL")
    List<Loan> findStaleAssignedLoans(@Param("status") Loan.LoanStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
}
