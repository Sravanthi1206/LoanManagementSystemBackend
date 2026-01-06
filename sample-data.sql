-- ============================================================
-- LMS Sample Data for Demonstration
-- Run this AFTER all services have started and created tables
-- ============================================================

-- ============================================================
-- 1. IDENTITY DATABASE (lms_identity)
-- ============================================================
USE lms_identity;

-- Password for all users: Password@123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W

-- Note: ROOT_ADMIN (id=1) is auto-created by DataInitializer
-- Insert other users starting from id=2

INSERT INTO users (id, email, password_hash, first_name, last_name, phone, role, active, approved, approval_pending, password_change_required, created_at, updated_at) VALUES
(2, 'admin@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'System', 'Admin', '+919876543210', 'ADMIN', true, true, false, false, NOW(), NOW()),
(3, 'officer1@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Rahul', 'Sharma', '+919876543211', 'LOAN_OFFICER', true, true, false, false, NOW(), NOW()),
(4, 'officer2@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Priya', 'Verma', '+919876543212', 'LOAN_OFFICER', true, true, false, false, NOW(), NOW()),
(5, 'customer1@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Amit', 'Kumar', '+919876543213', 'CUSTOMER', true, true, false, false, NOW(), NOW()),
(6, 'customer2@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Sneha', 'Patel', '+919876543214', 'CUSTOMER', true, true, false, false, NOW(), NOW()),
(7, 'customer3@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Vikram', 'Singh', '+919876543215', 'CUSTOMER', true, true, false, false, NOW(), NOW());

-- ============================================================
-- 2. LOAN DATABASE (lms_loan)
-- ============================================================
USE lms_loan;

-- Sample Loans with different statuses
INSERT INTO loans (id, user_id, loan_type, amount, tenure_months, interest_rate, purpose, employment_type, monthly_income, status, assigned_officer_id, remarks, created_at, updated_at) VALUES
-- Approved and Disbursed loan for customer1
(1, 5, 'PERSONAL', 500000.00, 36, 12.5, 'Home renovation and furniture purchase', 'SALARIED', 75000.00, 'DISBURSED', 3, 'All documents verified. Good credit score.', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),

-- Active loan for customer2
(2, 6, 'HOME', 2500000.00, 180, 8.5, 'First home purchase in Bangalore', 'SALARIED', 150000.00, 'DISBURSED', 3, 'Property verified. Title clear.', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 50 DAY)),

-- Pending review loan for customer3
(3, 7, 'VEHICLE', 800000.00, 60, 10.0, 'New car purchase - Honda City', 'SELF_EMPLOYED', 100000.00, 'UNDER_REVIEW', 4, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- Recently applied loan for customer1
(4, 5, 'EDUCATION', 1000000.00, 48, 9.0, 'MBA program at IIM Bangalore', 'SALARIED', 75000.00, 'APPLIED', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Rejected loan for customer2
(5, 6, 'PERSONAL', 2000000.00, 24, 14.0, 'Business expansion', 'SALARIED', 150000.00, 'REJECTED', 4, 'Loan amount too high for the income bracket.', DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 40 DAY)),

-- Another pending loan
(6, 7, 'PERSONAL', 300000.00, 24, 13.0, 'Medical emergency expenses', 'SELF_EMPLOYED', 100000.00, 'APPLIED', NULL, NULL, NOW(), NOW());

-- Wallet balances
INSERT INTO wallets (id, user_id, balance, created_at, updated_at) VALUES
(1, 5, 50000.00, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
(2, 6, 100000.00, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW()),
(3, 7, 25000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW());

-- Wallet transactions
INSERT INTO wallet_transactions (id, wallet_id, amount, transaction_type, description, created_at) VALUES
(1, 1, 100000.00, 'CREDIT', 'Initial deposit', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2, 1, -15000.00, 'DEBIT', 'EMI Payment - Loan #1', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(3, 1, -15000.00, 'DEBIT', 'EMI Payment - Loan #1', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 1, -20000.00, 'DEBIT', 'EMI Payment - Loan #1', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(5, 2, 200000.00, 'CREDIT', 'Initial deposit', DATE_SUB(NOW(), INTERVAL 60 DAY)),
(6, 2, -25000.00, 'DEBIT', 'EMI Payment - Loan #2', DATE_SUB(NOW(), INTERVAL 55 DAY)),
(7, 2, -25000.00, 'DEBIT', 'EMI Payment - Loan #2', DATE_SUB(NOW(), INTERVAL 50 DAY)),
(8, 2, -25000.00, 'DEBIT', 'EMI Payment - Loan #2', DATE_SUB(NOW(), INTERVAL 45 DAY)),
(9, 2, -25000.00, 'DEBIT', 'EMI Payment - Loan #2', DATE_SUB(NOW(), INTERVAL 40 DAY)),
(10, 3, 25000.00, 'CREDIT', 'Initial deposit', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ============================================================
-- 3. PAYMENT DATABASE (lms_payment)
-- ============================================================
USE lms_payment;

-- Payment records for disbursed loans
INSERT INTO payments (id, loan_id, user_id, amount, payment_type, transaction_id, status, created_at) VALUES
-- Disbursement payments
(1, 1, 5, 500000.00, 'DISBURSEMENT', 'TXN-DISB-001', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 2, 6, 2500000.00, 'DISBURSEMENT', 'TXN-DISB-002', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 50 DAY)),

-- EMI repayments for Loan 1 (customer1)
(3, 1, 5, 15000.00, 'REPAYMENT', 'TXN-EMI-001', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, 1, 5, 15000.00, 'REPAYMENT', 'TXN-EMI-002', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(5, 1, 5, 20000.00, 'REPAYMENT', 'TXN-EMI-003', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 15 DAY)),

-- EMI repayments for Loan 2 (customer2)
(6, 2, 6, 25000.00, 'REPAYMENT', 'TXN-EMI-004', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 55 DAY)),
(7, 2, 6, 25000.00, 'REPAYMENT', 'TXN-EMI-005', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 50 DAY)),
(8, 2, 6, 25000.00, 'REPAYMENT', 'TXN-EMI-006', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 45 DAY)),
(9, 2, 6, 25000.00, 'REPAYMENT', 'TXN-EMI-007', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 40 DAY));

-- ============================================================
-- SUMMARY OF TEST ACCOUNTS
-- ============================================================
-- All passwords: Password@123
--
-- ROOT_ADMIN: root@lms.com (auto-created by app)
-- ADMIN: admin@lms.com
-- LOAN_OFFICER: officer1@lms.com, officer2@lms.com
-- CUSTOMER: customer1@gmail.com, customer2@gmail.com, customer3@gmail.com
--
-- Loan Status Summary:
-- - Loan 1: DISBURSED (customer1, Personal, 5L)
-- - Loan 2: DISBURSED (customer2, Home, 25L)
-- - Loan 3: UNDER_REVIEW (customer3, Vehicle, 8L)
-- - Loan 4: APPLIED (customer1, Education, 10L)
-- - Loan 5: REJECTED (customer2, Personal, 20L)
-- - Loan 6: APPLIED (customer3, Personal, 3L)
-- ============================================================
