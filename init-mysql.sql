-- LMS Database Initialization Script

-- Create databases
CREATE DATABASE IF NOT EXISTS lms_users CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS lms_loans CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant permissions to lms_user
GRANT ALL PRIVILEGES ON lms_users.* TO 'lms_user'@'%';
GRANT ALL PRIVILEGES ON lms_loans.* TO 'lms_user'@'%';
FLUSH PRIVILEGES;

-- Switch to lms_users database
USE lms_users;

-- Users table (will be auto-created by JPA, but we define structure here)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    date_of_birth DATE,
    pan_card VARCHAR(10),
    role ENUM('CUSTOMER', 'LOAN_OFFICER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Insert test users (password is BCrypt hash of 'Password@123')
INSERT INTO users (email, password_hash, first_name, last_name, phone, role, active) VALUES
('customer@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoHK.MzHqQMJTy5sO9Wj7X6EPlb5sD5a5O3O', 'Test', 'Customer', '+919876543210', 'CUSTOMER', TRUE),
('officer@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoHK.MzHqQMJTy5sO9Wj7X6EPlb5sD5a5O3O', 'Test', 'Officer', '+919876543211', 'LOAN_OFFICER', TRUE),
('admin@lms.com', '$2a$10$N9qo8uLOickgx2ZMRZoHK.MzHqQMJTy5sO9Wj7X6EPlb5sD5a5O3O', 'Test', 'Admin', '+919876543212', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE email=email;

-- Switch to lms_loans database  
USE lms_loans;

-- Loan applications table
CREATE TABLE IF NOT EXISTS loan_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    loan_type ENUM('PERSONAL', 'HOME', 'VEHICLE', 'EDUCATION', 'BUSINESS') NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    approved_amount DECIMAL(15,2),
    interest_rate DECIMAL(5,2),
    tenure_months INT NOT NULL,
    emi_amount DECIMAL(15,2),
    purpose TEXT,
    employment_type ENUM('SALARIED', 'SELF_EMPLOYED', 'BUSINESS') NOT NULL,
    employer_name VARCHAR(200),
    monthly_income DECIMAL(15,2) NOT NULL,
    annual_income DECIMAL(15,2) NOT NULL,
    existing_loans BOOLEAN DEFAULT FALSE,
    existing_emi_amount DECIMAL(15,2) DEFAULT 0,
    pan_card VARCHAR(10) NOT NULL,
    status ENUM('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'DISBURSED', 'ACTIVE', 'CLOSED') NOT NULL DEFAULT 'DRAFT',
    credit_score INT,
    assigned_officer_id BIGINT,
    remarks TEXT,
    rejection_reason TEXT,
    submitted_at TIMESTAMP,
    approved_at TIMESTAMP,
    disbursed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_application_number (application_number)
);

-- Document storage table
CREATE TABLE IF NOT EXISTS application_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    document_type ENUM('IDENTITY_PROOF', 'ADDRESS_PROOF', 'INCOME_PROOF', 'BANK_STATEMENT', 'PAN_CARD', 'PHOTO') NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES loan_applications(id) ON DELETE CASCADE,
    INDEX idx_application_id (application_id)
);

-- Virtual wallet table for demo
CREATE TABLE IF NOT EXISTS user_wallet (
    user_id BIGINT PRIMARY KEY,
    balance DECIMAL(15,2) DEFAULT 100000.00,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Initialize wallets for test users (user IDs 1, 2, 3 from lms_users)
INSERT INTO user_wallet (user_id, balance) VALUES 
(1, 100000.00)
ON DUPLICATE KEY UPDATE user_id=user_id;

-- Wallet transactions table
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    loan_id BIGINT,
    type ENUM('CREDIT', 'DEBIT', 'DISBURSEMENT', 'EMI_PAYMENT', 'PENALTY', 'REFUND') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_loan_id (loan_id),
    INDEX idx_type (type)
);

SELECT 'LMS Database initialization complete!' AS Status;
