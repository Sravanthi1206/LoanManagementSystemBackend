# 🏦 LOAN MANAGEMENT SYSTEM

**A Secure Full-Stack Microservices Application**

**Author:** Sravanthi Gurram  
**Date:** January 2026

---

## 📋 Problem Statement

### The Challenge
- Manual, paper-heavy loan processing
- Lack of real-time tracking
- Fragmented systems
- Security vulnerabilities
- Poor customer experience

### Our Solution
A modern, secure, **microservices-based** Loan Management System that automates the complete loan lifecycle.

`Automated` • `Secure` • `Scalable`

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| 🔐 **Secure Authentication** | JWT-based login with role-based access control |
| 📝 **Loan Application** | Apply for various loan types with validation |
| ⚡ **EMI Calculator** | Real-time EMI calculation and schedule generation |
| 💳 **Virtual Wallet** | Simulated disbursement & payment system |
| 📧 **Notifications** | Email alerts for all loan status changes |
| 👥 **Multi-Role Support** | Customer, Loan Officer, Admin, ROOT_ADMIN |

---

## 🏗 System Architecture

```
                         ┌─────────────────┐
                         │  Angular Client │
                         └────────┬────────┘
                                  │
                         ┌────────▼────────┐
                         │   API Gateway   │
                         │     :8080       │
                         └────────┬────────┘
                                  │
    ┌──────────┬──────────┬───────┴───────┬──────────┐
    ▼          ▼          ▼               ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐ ┌────────────┐
│Identity│ │  Loan  │ │  EMI   │ │ Payment  │ │Notification│
│ :8081  │ │ :8082  │ │ :8083  │ │  :8084   │ │   :8085    │
└────────┘ └────────┘ └────────┘ └──────────┘ └────────────┘
    │          │          │           │            │
    └──────────┴──────────┼───────────┴────────────┘
                          │
              ┌───────────┴───────────┐
              │   MySQL    │  MongoDB │
              │ (4 DBs)    │ (1 DB)   │
              └───────────────────────┘
```

### 📐 [View Full Architecture Diagram](diagrams/ArchitectureDiagram.png)

---

## 🔑 Architecture Highlights

| Component | Purpose |
|-----------|---------|
| **API Gateway** | Single entry point, JWT validation, routing |
| **Eureka Server** | Service discovery & registration |
| **Config Server** | Centralized configuration from Git |
| **RabbitMQ** | Async event-driven notifications |
| **Polyglot DB** | MySQL for transactions, MongoDB for notifications |

---

## 🛠 Technology Stack

### Backend
- Spring Boot 3.3
- Spring Cloud (Gateway, Eureka, Config)
- Spring Security + JWT
- Spring Data JPA / MongoDB
- RabbitMQ

### Frontend & DevOps
- Angular 17 + TypeScript
- Docker & Docker Compose
- Jenkins CI/CD
- SonarCloud + JaCoCo
- MySQL 8.0 + MongoDB 6.0

---

## 📦 Microservices Overview

| Service | Port | Database | Responsibilities |
|---------|------|----------|-----------------|
| Identity | 8081 | MySQL | Authentication, JWT, RBAC |
| Loan | 8082 | MySQL | Applications, Wallet, Credit Check |
| EMI | 8083 | MySQL | Calculation, Schedule Generation |
| Payment | 8084 | MySQL | Transaction Recording |
| Notification | 8085 | MongoDB | Email Alerts, Event Consumption |

### Infrastructure Services
- **Config Server** `:8888`
- **Eureka** `:8761`
- **API Gateway** `:8080`

---

## 🗄 Database Design

### MySQL (4 Databases)
- **lms_identity:** users
- **lms_loans:** loan_applications, user_wallet, wallet_transactions, documents
- **lms_emi:** repayment_schedules
- **lms_payments:** payments

### MongoDB (1 Database)
- **lms_notifications:** notifications collection

### Key Relationships
```
users (1) ─── (*) loans
loans (1) ─── (*) repayment_schedules
loans (1) ─── (*) payments
```

### 📐 [View ER Diagram](diagrams/ERDiagram.png)

---

## 📊 Loan Status Flow

```
    ┌─────────┐      ┌──────────────┐      ┌──────────┐
    │ APPLIED │ ───▶ │ UNDER_REVIEW │ ───▶ │ APPROVED │
    └─────────┘      └──────┬───────┘      └────┬─────┘
                           │                    │
                           ▼                    ▼
                     ┌──────────┐         ┌───────────┐
                     │ REJECTED │         │ DISBURSED │
                     └──────────┘         └─────┬─────┘
                                                │
                                                ▼
                                          ┌──────────┐
                                          │  CLOSED  │
                                          └──────────┘
```

✅ Credit Check Required • ✅ EMI Schedule Generated • ✅ Notifications Sent

### 📐 [View Sequence Diagram](diagrams/Complete_Application_Flow.png)

---

## 🔐 Security Implementation

### JWT Authentication
- BCrypt password hashing
- Stateless token-based auth
- Role-based access control

### Security Layers
1. **Gateway:** JWT validation
2. **Service:** @PreAuthorize
3. **Data:** User-scoped queries

### RBAC Matrix

| Role | Apply Loan | Approve | Create Staff |
|------|------------|---------|--------------|
| Customer | ✅ | ❌ | ❌ |
| Loan Officer | ❌ | ✅ | ❌ |
| Admin | ❌ | ✅ | ✅ |

---

## 🔄 CI/CD Pipeline

```
┌──────────┐    ┌─────────────┐    ┌────────────┐    ┌────────────┐    ┌────────┐
│ Checkout │───▶│ Build/Test  │───▶│ SonarCloud │───▶│Docker Build│───▶│ Deploy │
└──────────┘    └─────────────┘    └────────────┘    └────────────┘    └────────┘
     │               │                   │                 │               │
  GitHub         JUnit Tests       Quality Gates      Multi-image      Compose
                 JaCoCo Reports    Security Scan      Build            Up
```

### Quality Gates
- ✅ Coverage > 80%
- ✅ No Critical Bugs
- ✅ No Vulnerabilities

### 📐 [View CI/CD Diagram](diagrams/ci-cd_pipeline%20flow.png)

---

## 📜 Business Rules

| Rule | Description |
|------|-------------|
| Minimum Income | Monthly income ≥ ₹25,000 required |
| EMI-to-Income Ratio | EMI ≤ 40% of monthly income |
| Debt-to-Income Ratio | Total debt ≤ 50% of monthly income |
| Credit Check | Score based on employment type & income |
| Risk Categories | LOW (≥750) \| MEDIUM (650-749) \| HIGH (<650) |
| Approval Required | Credit check must pass before approval |

### Status Transition Rules
- APPLIED → UNDER_REVIEW
- UNDER_REVIEW → APPROVED/REJECTED
- APPROVED → DISBURSED

---

## 🎯 Challenges & Solutions

| Challenge | Solution |
|-----------|----------|
| Inter-service Communication | Feign clients with Resilience4j circuit breakers |
| Distributed Transactions | Eventual consistency via RabbitMQ events |
| Configuration Management | Spring Cloud Config Server with Git backend |
| Service Discovery | Eureka for dynamic registration |

### Key Learnings
- 📌 API-first design
- 📌 Event-driven architecture
- 📌 Test early & often

---

## 🚀 Future Scope

### Phase 2
- 📱 Mobile App (Flutter)
- 💳 Payment Gateway (Razorpay)
- 📊 Analytics Dashboard

### Phase 3
- 🤖 AI Credit Scoring
- ☁️ Cloud Deployment (AWS/Azure)
- 🔄 Kubernetes Auto-scaling

---

## 🙏 Thank You!

### Questions?

**Sravanthi Gurram**

🔗 [github.com/Sravanthi1206](https://github.com/Sravanthi1206)  
📧 sravanthigurram955@gmail.com
