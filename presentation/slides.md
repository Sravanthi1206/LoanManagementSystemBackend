---
marp: true
theme: default
paginate: true
backgroundColor: #ffffff
style: |
  section {
    font-family: 'Segoe UI', Arial, sans-serif;
  }
  h1 {
    color: #1a1a2e;
  }
  h2 {
    color: #1a1a2e;
    border-bottom: 2px solid #667eea;
    padding-bottom: 10px;
  }
  table {
    font-size: 0.8em;
  }
  .columns {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
---

<!-- _class: lead -->
<!-- _backgroundColor: linear-gradient(135deg, #667eea 0%, #764ba2 100%) -->
<!-- _color: white -->

# 🏦 LOAN MANAGEMENT SYSTEM

### A Secure Full-Stack Microservices Application

**Sravanthi Gurram**
January 2026

---

## 📋 Problem Statement

<div class="columns">
<div>

### The Challenge ❌
- Manual, paper-heavy loan processing
- Lack of real-time tracking
- Fragmented systems
- Security vulnerabilities
- Poor customer experience

</div>
<div>

### Our Solution ✅
A modern, secure, **microservices-based** Loan Management System that automates the complete loan lifecycle.

🟢 Automated | 🔵 Secure | 🟣 Scalable

</div>
</div>

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

![height:400px](diagrams/ArchitectureDiagram.png)

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

<div class="columns">
<div>

### Backend
- Spring Boot 3.3
- Spring Cloud (Gateway, Eureka, Config)
- Spring Security + JWT
- Spring Data JPA / MongoDB
- RabbitMQ

</div>
<div>

### Frontend & DevOps
- Angular 17 + TypeScript
- Docker & Docker Compose
- Jenkins CI/CD
- SonarCloud + JaCoCo
- MySQL 8.0 + MongoDB 6.0

</div>
</div>

---

## 📦 Microservices Overview

| Service | Port | Database | Responsibilities |
|---------|------|----------|-----------------|
| Identity | 8081 | MySQL | Authentication, JWT, RBAC |
| Loan | 8082 | MySQL | Applications, Wallet, Credit Check |
| EMI | 8083 | MySQL | Calculation, Schedule Generation |
| Payment | 8084 | MySQL | Transaction Recording |
| Notification | 8085 | MongoDB | Email Alerts, Event Consumption |

**Infrastructure:** Config Server `:8888` • Eureka `:8761` • Gateway `:8080`

---

## 🗄 Database Design

![height:400px](diagrams/ERDiagram.png)

---

## 📊 Loan Status Flow

![height:400px](diagrams/Complete_Application_Flow.png)

---

## 🔐 Security Implementation

<div class="columns">
<div>

### JWT Authentication
- BCrypt password hashing
- Stateless token-based auth
- Role-based access control

</div>
<div>

### RBAC Matrix
| Role | Apply | Approve | Create Staff |
|------|-------|---------|--------------|
| Customer | ✅ | ❌ | ❌ |
| Loan Officer | ❌ | ✅ | ❌ |
| Admin | ❌ | ✅ | ✅ |

</div>
</div>

---

## 🔄 CI/CD Pipeline

![height:350px](diagrams/ci-cd_pipeline%20flow.png)

### Quality Gates
✅ Coverage > 80% • ✅ No Critical Bugs • ✅ No Vulnerabilities

---

## 📜 Business Rules

| Rule | Description |
|------|-------------|
| Minimum Income | Monthly income ≥ ₹25,000 required |
| EMI-to-Income Ratio | EMI ≤ 40% of monthly income |
| Debt-to-Income Ratio | Total debt ≤ 50% of monthly income |
| Credit Check | Score based on employment type & income |
| Risk Categories | LOW (≥750) \| MEDIUM (650-749) \| HIGH (<650) |

---

## 🎯 Challenges & Solutions

| Challenge | Solution |
|-----------|----------|
| Inter-service Communication | Feign clients with Resilience4j circuit breakers |
| Distributed Transactions | Eventual consistency via RabbitMQ events |
| Configuration Management | Spring Cloud Config Server with Git backend |
| Service Discovery | Eureka for dynamic registration |

### Key Learnings
📌 API-first design • 📌 Event-driven architecture • 📌 Test early & often

---

## 🚀 Future Scope

<div class="columns">
<div>

### Phase 2
- 📱 Mobile App (Flutter)
- 💳 Payment Gateway (Razorpay)
- 📊 Analytics Dashboard

</div>
<div>

### Phase 3
- 🤖 AI Credit Scoring
- ☁️ Cloud Deployment (AWS/Azure)
- 🔄 Kubernetes Auto-scaling

</div>
</div>

---

<!-- _class: lead -->
<!-- _backgroundColor: linear-gradient(135deg, #667eea 0%, #764ba2 100%) -->
<!-- _color: white -->

# Thank You! 🙏

### Questions?

**Sravanthi Gurram**
🔗 github.com/Sravanthi1206
📧 sravanthigurram955@gmail.com
