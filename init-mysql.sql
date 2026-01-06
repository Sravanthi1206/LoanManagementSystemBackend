-- ============================================================
-- LMS Database Initialization Script
-- Used by Docker and for local setup
-- Schema matches JPA entity definitions exactly
-- ============================================================

-- Drop existing databases for clean start
DROP DATABASE IF EXISTS lms_identity;
DROP DATABASE IF EXISTS lms_loan;
DROP DATABASE IF EXISTS lms_payment;
DROP DATABASE IF EXISTS lms_emi;

-- Create databases
CREATE DATABASE lms_identity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE lms_loan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE lms_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE lms_emi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant permissions to lms_user (for Docker)
GRANT ALL PRIVILEGES ON lms_identity.* TO 'lms_user'@'%';
GRANT ALL PRIVILEGES ON lms_loan.* TO 'lms_user'@'%';
GRANT ALL PRIVILEGES ON lms_payment.* TO 'lms_user'@'%';
GRANT ALL PRIVILEGES ON lms_emi.* TO 'lms_user'@'%';
FLUSH PRIVILEGES;

-- ============================================================
-- IDENTITY DATABASE
-- Matches: com.lms.identity.entity.User
-- ============================================================
USE lms_identity;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    date_of_birth DATE,
    pan_card VARCHAR(10) UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    active BOOLEAN DEFAULT TRUE,
    password_change_required BOOLEAN DEFAULT FALSE,
    approved BOOLEAN DEFAULT TRUE,
    approval_pending BOOLEAN DEFAULT FALSE,
    created_by_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Password for all: Password@123
-- BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W
INSERT INTO users (id, email, password_hash, first_name, last_name, phone, date_of_birth, pan_card, role, active, approved, approval_pending, password_change_required) VALUES
-- Root Admin
(1, 'root@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Root', 'Admin', '+919999999999', NULL, NULL, 'ROOT_ADMIN', true, true, false, false),
-- Admins (one pending approval for workflow demo)
(2, 'admin@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'System', 'Admin', '+919876543210', NULL, NULL, 'ADMIN', true, true, false, false),
(3, 'newadmin@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Pending', 'Admin', '+919876543299', NULL, NULL, 'ADMIN', false, false, true, true),
-- Loan Officers
(4, 'officer1@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Rahul', 'Sharma', '+919876543211', NULL, NULL, 'LOAN_OFFICER', true, true, false, false),
(5, 'officer2@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Priya', 'Verma', '+919876543212', NULL, NULL, 'LOAN_OFFICER', true, true, false, false),
(6, 'officer3@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Amit', 'Gupta', '+919876543298', NULL, NULL, 'LOAN_OFFICER', true, true, false, false),
-- Active Customers (with DOB and PAN for loan eligibility)
(7, 'customer1@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Amit', 'Kumar', '+919876543213', '1990-05-15', 'ABCDE1234F', 'CUSTOMER', true, true, false, false),
(8, 'customer2@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Sneha', 'Patel', '+919876543214', '1988-08-22', 'FGHIJ5678K', 'CUSTOMER', true, true, false, false),
(9, 'customer3@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Vikram', 'Singh', '+919876543215', '1992-12-10', 'KLMNO9012P', 'CUSTOMER', true, true, false, false),
-- Inactive Customer (for activation demo)
(10, 'inactive@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'Raj', 'Inactive', '+919876543216', '1985-03-25', 'QRSTU3456V', 'CUSTOMER', false, true, false, false),
-- Customer without profile (for profile update demo)
(11, 'newcustomer@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye6VQn/S9nVxNNknnInZqBxmJdJMC9f7W', 'New', 'Customer', '+919876543217', NULL, NULL, 'CUSTOMER', true, true, false, false);

-- ============================================================
-- LOAN DATABASE
-- Matches: com.lms.loan.entity.Loan (table: loan_applications)
-- Matches: com.lms.loan.entity.UserWallet (table: user_wallet)
-- Matches: com.lms.loan.entity.WalletTransaction (table: wallet_transactions)
-- ============================================================
USE lms_loan;

-- Table: loan_applications (matches Loan.java entity exactly)
CREATE TABLE loan_applications (
    loan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_email VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    amount_requested DECIMAL(15,2) NOT NULL,
    tenure_months INT NOT NULL,
    purpose VARCHAR(500),
    employment_type VARCHAR(50),
    employer_name VARCHAR(200),
    monthly_income DECIMAL(15,2),
    annual_income DECIMAL(15,2),
    existing_loans BOOLEAN DEFAULT FALSE,
    existing_emi_amount DECIMAL(15,2) DEFAULT 0.00,
    interest_rate DECIMAL(5,2),
    amount_approved DECIMAL(15,2),
    status VARCHAR(50) NOT NULL DEFAULT 'APPLIED',
    officer_remarks TEXT,
    assigned_officer_id BIGINT,
    assigned_at TIMESTAMP,
    credit_score INT,
    risk_category VARCHAR(50),
    applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_on TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_officer (assigned_officer_id)
);

-- Table: user_wallet (matches UserWallet.java entity exactly)
CREATE TABLE user_wallet (
    user_id BIGINT PRIMARY KEY,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table: wallet_transactions (matches WalletTransaction.java entity exactly)
CREATE TABLE wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    loan_id BIGINT,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_loan_id (loan_id)
);

-- Table: application_documents (matches ApplicationDocument.java entity)
CREATE TABLE application_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    content_type VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_loan_id (loan_id)
);

-- Sample loan applications (user IDs match new schema: customers=7,8,9, officers=4,5,6)
INSERT INTO loan_applications (loan_id, user_id, user_email, type, amount_requested, tenure_months, purpose, employment_type, monthly_income, status, interest_rate, amount_approved, assigned_officer_id, officer_remarks, applied_on, approved_on, credit_score, risk_category) VALUES
-- DISBURSED: customer1's personal loan (fully processed)
(1, 7, 'customer1@gmail.com', 'PERSONAL', 500000.00, 36, 'Home renovation', 'SALARIED', 75000.00, 'DISBURSED', 12.50, 500000.00, 4, 'Good credit history', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY), 750, 'LOW'),
-- DISBURSED: customer2's home loan (fully processed)
(2, 8, 'customer2@gmail.com', 'HOME', 2500000.00, 180, 'First home purchase', 'SALARIED', 150000.00, 'DISBURSED', 8.50, 2500000.00, 4, 'Property verified', DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 55 DAY), 780, 'LOW'),
-- UNDER_REVIEW: customer3's vehicle loan (assigned to officer2)
(3, 9, 'customer3@gmail.com', 'VEHICLE', 800000.00, 60, 'New car purchase', 'SELF_EMPLOYED', 100000.00, 'UNDER_REVIEW', NULL, NULL, 5, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, 680, 'MEDIUM'),
-- APPLIED: customer1's education loan (unassigned - in pool)
(4, 7, 'customer1@gmail.com', 'EDUCATION', 1000000.00, 48, 'MBA program', 'SALARIED', 75000.00, 'APPLIED', NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL, NULL),
-- REJECTED: customer2's personal loan (high risk)
(5, 8, 'customer2@gmail.com', 'PERSONAL', 2000000.00, 24, 'Business expansion', 'SALARIED', 150000.00, 'REJECTED', NULL, NULL, 5, 'Income insufficient for requested amount', DATE_SUB(NOW(), INTERVAL 45 DAY), NULL, 620, 'HIGH'),
-- APPLIED: customer3's personal loan (unassigned - in pool)
(6, 9, 'customer3@gmail.com', 'PERSONAL', 300000.00, 24, 'Medical expenses', 'SELF_EMPLOYED', 100000.00, 'APPLIED', NULL, NULL, NULL, NULL, NOW(), NULL, NULL, NULL),
-- APPROVED: customer1's vehicle loan (pending disbursement)
(7, 7, 'customer1@gmail.com', 'VEHICLE', 600000.00, 48, 'Bike purchase', 'SALARIED', 75000.00, 'APPROVED', 9.75, 600000.00, 6, 'Approved - awaiting disbursement', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 750, 'LOW');

-- Sample wallets (updated user IDs: 7, 8, 9)
INSERT INTO user_wallet (user_id, balance) VALUES
(7, 50000.00),
(8, 100000.00),
(9, 25000.00);

-- Sample wallet transactions (updated user IDs)
INSERT INTO wallet_transactions (transaction_id, user_id, loan_id, type, amount, balance_after, description, created_at) VALUES
('TXN-INIT-001', 7, NULL, 'TOP_UP', 100000.00, 100000.00, 'Initial deposit', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('TXN-EMI-001', 7, 1, 'EMI_PAYMENT', -15000.00, 85000.00, 'EMI #1 Payment', DATE_SUB(NOW(), INTERVAL 25 DAY)),
('TXN-EMI-002', 7, 1, 'EMI_PAYMENT', -15000.00, 70000.00, 'EMI #2 Payment', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('TXN-EMI-003', 7, 1, 'EMI_PAYMENT', -20000.00, 50000.00, 'EMI #3 Payment', DATE_SUB(NOW(), INTERVAL 15 DAY)),
('TXN-INIT-002', 8, NULL, 'TOP_UP', 200000.00, 200000.00, 'Initial deposit', DATE_SUB(NOW(), INTERVAL 60 DAY)),
('TXN-EMI-004', 8, 2, 'EMI_PAYMENT', -25000.00, 175000.00, 'EMI #1 Payment', DATE_SUB(NOW(), INTERVAL 55 DAY)),
('TXN-EMI-005', 8, 2, 'EMI_PAYMENT', -25000.00, 150000.00, 'EMI #2 Payment', DATE_SUB(NOW(), INTERVAL 50 DAY)),
('TXN-EMI-006', 8, 2, 'EMI_PAYMENT', -25000.00, 125000.00, 'EMI #3 Payment', DATE_SUB(NOW(), INTERVAL 45 DAY)),
('TXN-EMI-007', 8, 2, 'EMI_PAYMENT', -25000.00, 100000.00, 'EMI #4 Payment', DATE_SUB(NOW(), INTERVAL 40 DAY)),
('TXN-INIT-003', 9, NULL, 'TOP_UP', 25000.00, 25000.00, 'Initial deposit', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ============================================================
-- EMI DATABASE
-- Matches: com.lms.emi.entity.RepaymentSchedule (table: repayment_schedules)
-- ============================================================
USE lms_emi;

CREATE TABLE repayment_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    user_id BIGINT,
    installment_no INT,
    due_date DATE,
    principal_amount DECIMAL(15,2),
    interest_amount DECIMAL(15,2),
    total_emi DECIMAL(15,2),
    status VARCHAR(50) DEFAULT 'PENDING',
    paid_date DATE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);

-- Sample EMI schedules for loan #1 (PERSONAL 500000, 36 months, 12.5%) - user_id=7
INSERT INTO repayment_schedules (loan_id, user_id, installment_no, due_date, principal_amount, interest_amount, total_emi, status, paid_date) VALUES
(1, 7, 1, DATE_SUB(NOW(), INTERVAL 25 DAY), 11458.33, 5208.33, 16666.67, 'PAID', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 7, 2, DATE_SUB(NOW(), INTERVAL 20 DAY), 11577.82, 5088.84, 16666.67, 'PAID', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 7, 3, DATE_SUB(NOW(), INTERVAL 15 DAY), 11698.69, 4967.98, 16666.67, 'PAID', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 7, 4, DATE_ADD(NOW(), INTERVAL 15 DAY), 11820.94, 4845.72, 16666.67, 'PENDING', NULL),
(1, 7, 5, DATE_ADD(NOW(), INTERVAL 45 DAY), 11944.61, 4722.06, 16666.67, 'PENDING', NULL);

-- Sample EMI schedules for loan #2 (HOME 2500000, 180 months, 8.5%) - user_id=8
INSERT INTO repayment_schedules (loan_id, user_id, installment_no, due_date, principal_amount, interest_amount, total_emi, status, paid_date) VALUES
(2, 8, 1, DATE_SUB(NOW(), INTERVAL 55 DAY), 6729.17, 17708.33, 24437.50, 'PAID', DATE_SUB(NOW(), INTERVAL 55 DAY)),
(2, 8, 2, DATE_SUB(NOW(), INTERVAL 50 DAY), 6776.84, 17660.66, 24437.50, 'PAID', DATE_SUB(NOW(), INTERVAL 50 DAY)),
(2, 8, 3, DATE_SUB(NOW(), INTERVAL 45 DAY), 6824.82, 17612.68, 24437.50, 'PAID', DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2, 8, 4, DATE_SUB(NOW(), INTERVAL 40 DAY), 6873.15, 17564.35, 24437.50, 'PAID', DATE_SUB(NOW(), INTERVAL 40 DAY)),
(2, 8, 5, DATE_ADD(NOW(), INTERVAL 20 DAY), 6921.82, 17515.68, 24437.50, 'PENDING', NULL);

-- ============================================================
-- PAYMENT DATABASE
-- Matches: com.lms.payment.entity.Payment (table: payments)
-- ============================================================
USE lms_payment;

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_method VARCHAR(20),
    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',
    transaction_id VARCHAR(100) UNIQUE,
    reference_number VARCHAR(100),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_id (loan_id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_id (transaction_id)
);

-- Sample payments (updated user IDs)
INSERT INTO payments (loan_id, user_id, payment_type, amount, payment_method, status, transaction_id, payment_date, created_at) VALUES
(1, 7, 'DISBURSEMENT', 500000.00, 'BANK_TRANSFER', 'SUCCESS', 'TXN-DISB-001', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 8, 'DISBURSEMENT', 2500000.00, 'BANK_TRANSFER', 'SUCCESS', 'TXN-DISB-002', DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 50 DAY)),
(1, 7, 'EMI_REPAYMENT', 16666.67, 'WALLET', 'SUCCESS', 'TXN-PAY-001', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 7, 'EMI_REPAYMENT', 16666.67, 'WALLET', 'SUCCESS', 'TXN-PAY-002', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 7, 'EMI_REPAYMENT', 16666.67, 'WALLET', 'SUCCESS', 'TXN-PAY-003', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 8, 'EMI_REPAYMENT', 24437.50, 'WALLET', 'SUCCESS', 'TXN-PAY-004', DATE_SUB(NOW(), INTERVAL 55 DAY), DATE_SUB(NOW(), INTERVAL 55 DAY)),
(2, 8, 'EMI_REPAYMENT', 24437.50, 'WALLET', 'SUCCESS', 'TXN-PAY-005', DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 50 DAY)),
(2, 8, 'EMI_REPAYMENT', 24437.50, 'WALLET', 'SUCCESS', 'TXN-PAY-006', DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2, 8, 'EMI_REPAYMENT', 24437.50, 'WALLET', 'SUCCESS', 'TXN-PAY-007', DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 40 DAY));

-- ============================================================
-- DEMO SCENARIOS SUMMARY (Password for all: Password@123)
-- ============================================================
--
-- USERS (11 total):
-- ID | Email                  | Role         | Status  | Demo Purpose
-- 1  | root@lms.com           | ROOT_ADMIN   | Active  | Root admin - can approve pending admins
-- 2  | admin@lms.com          | ADMIN        | Active  | Regular admin - limited permissions
-- 3  | newadmin@lms.com       | ADMIN        | Pending | Pending approval by ROOT_ADMIN
-- 4  | officer1@lms.com       | LOAN_OFFICER | Active  | Has assigned loans
-- 5  | officer2@lms.com       | LOAN_OFFICER | Active  | Has assigned loans  
-- 6  | officer3@lms.com       | LOAN_OFFICER | Active  | Has assigned loans
-- 7  | customer1@gmail.com    | CUSTOMER     | Active  | Has loans in multiple states, has DOB/PAN
-- 8  | customer2@gmail.com    | CUSTOMER     | Active  | Has loans, has DOB/PAN
-- 9  | customer3@gmail.com    | CUSTOMER     | Active  | Has loans under review, has DOB/PAN
-- 10 | inactive@gmail.com     | CUSTOMER     | Inactive| For activation demo
-- 11 | newcustomer@gmail.com  | CUSTOMER     | Active  | No DOB/PAN - profile update demo
--
-- LOANS (7 total - all statuses covered):
-- #1  | customer1 | PERSONAL   | DISBURSED    | Fully processed, EMI payments ongoing
-- #2  | customer2 | HOME       | DISBURSED    | Fully processed, EMI payments ongoing
-- #3  | customer3 | VEHICLE    | UNDER_REVIEW | Assigned to officer2, pending decision
-- #4  | customer1 | EDUCATION  | APPLIED      | In pool, unassigned - officers can pick
-- #5  | customer2 | PERSONAL   | REJECTED     | High risk rejection example
-- #6  | customer3 | PERSONAL   | APPLIED      | In pool, unassigned - officers can pick
-- #7  | customer1 | VEHICLE    | APPROVED     | Pending disbursement
--
-- DEMO FLOWS:
-- 1. Customer applies: Login as newcustomer@gmail.com -> profile incomplete -> update profile -> apply
-- 2. Officer reviews: Login as officer1@lms.com -> pick loan #4 or #6 from pool -> review
-- 3. Admin manages: Login as root@lms.com -> approve pending admin newadmin@lms.com
-- 4. User activation: Login as admin@lms.com -> activate inactive@gmail.com
-- 5. EMI payment: Login as customer1@gmail.com -> view EMI schedule -> pay pending EMI
-- ============================================================

SELECT 'LMS Database initialization complete!' AS Status;
