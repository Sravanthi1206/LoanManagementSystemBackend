# 🏦 LMS Backend

Spring Boot Microservices Backend for the Loan Management System.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)](https://openjdk.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker)](https://www.docker.com/)

---

## 🏗 Architecture

```
                              ┌─────────────────┐
                              │  Client / User  │
                              └────────┬────────┘
                                       │ HTTPS
                              ┌────────▼────────┐
                              │   API Gateway   │
                              │     :8080       │
                              │ JWT • Routing   │
                              └────────┬────────┘
                                       │
       ┌───────────┬───────────┬───────┴───────┬───────────┐
       ▼           ▼           ▼               ▼           ▼
┌─────────────┐ ┌─────────┐ ┌───────┐ ┌─────────────┐ ┌────────────┐
│  Identity   │ │  Loan   │ │  EMI  │ │   Payment   │ │Notification│
│   :8081     │ │  :8082  │ │ :8083 │ │    :8084    │ │   :8085    │
└──────┬──────┘ └────┬────┘ └───┬───┘ └──────┬──────┘ └─────┬──────┘
       │             │          │            │              │
       ▼             ▼          ▼            ▼              ▼
┌───────────────────────────────────────────┐ ┌──────────────────┐
│                 MySQL                     │ │     MongoDB      │
│    identity • loans • emi • payments      │ │  notifications   │
└───────────────────────────────────────────┘ └──────────────────┘
                                       │
                              ┌────────▼────────┐
                              │    RabbitMQ     │
                              │   Event Bus     │
                              └─────────────────┘
```

---

## 📦 Services

### Infrastructure Services

| Service | Port | Technology | Purpose |
|---------|------|------------|---------|
| **Config Server** | 8888 | Spring Cloud Config | Centralized configuration from Git |
| **Discovery Server** | 8761 | Netflix Eureka | Service registration & discovery |
| **API Gateway** | 8080 | Spring Cloud Gateway | Routing, JWT validation, load balancing |

### Business Services

| Service | Port | Database | Responsibilities |
|---------|------|----------|-----------------|
| **Identity Service** | 8081 | MySQL (`lms_identity`) | User registration, authentication, JWT tokens, RBAC |
| **Loan Service** | 8082 | MySQL (`lms_loans`) | Loan applications, document upload, wallet, credit check, disbursement |
| **EMI Service** | 8083 | MySQL (`lms_emi`) | EMI calculation, schedule generation, payment tracking |
| **Payment Service** | 8084 | MySQL (`lms_payments`) | Transaction recording, wallet operations |
| **Notification Service** | 8085 | MongoDB (`lms_notifications`) | Email alerts, event consumption, templates |

---

## 🚀 Quick Start

### Prerequisites

- ☕ Java 17+
- 🐳 Docker & Docker Compose
- 📦 Maven 3.9+

### Option 1: Docker Compose (Recommended)

```bash
# Copy environment file
cp .env.example .env

# Edit .env with your credentials
# nano .env

# Build and run all services
docker compose up -d --build

# Check status
docker compose ps

# View logs
docker compose logs -f api-gateway
```

### Option 2: Local Development

```bash
# 1. Start databases only
docker compose up -d mysql mongodb rabbitmq

# 2. Build all services
mvn clean package -DskipTests

# 3. Start services using batch script (Windows)
start-local.bat

# Or start manually in order:
# config-server → discovery-server → api-gateway → business services
```

### Verify Startup

| Service | Health Check URL |
|---------|-----------------|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080/actuator/health |
| Config Server | http://localhost:8888/actuator/health |

---

## 📁 Project Structure

```
Backend/
├── api-gateway/              # Spring Cloud Gateway
├── config-server/            # Spring Cloud Config
├── discovery-server/         # Netflix Eureka
├── identity-service/         # Authentication & Users
│   └── src/main/java/
│       ├── controller/       # REST endpoints
│       ├── service/          # Business logic
│       ├── repository/       # Data access
│       └── security/         # JWT, RBAC
├── loan-service/             # Loan Processing & Wallet
├── emi-service/              # EMI Calculation
├── payment-service/          # Payment Recording
├── notification-service/     # Email Notifications
├── config-repo/              # Git-based configs
├── docker-compose.yml        # Full stack definition
├── Jenkinsfile               # CI/CD pipeline
├── init-mysql.sql            # Database initialization
├── .env                      # Environment variables
├── start-local.bat           # Local startup script
└── pom.xml                   # Parent Maven POM
```

---

## 🔌 API Endpoints

All APIs are accessed through the **API Gateway** at `http://localhost:8080`

### Authentication (`/auth`)

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/auth/register` | Public | Register new customer |
| POST | `/auth/login` | Public | Login & get JWT |
| POST | `/auth/change-password` | Authenticated | Change password |
| GET | `/auth/me` | Authenticated | Get current user profile |

### Loans (`/loans`)

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/loans/apply` | Customer | Submit loan application |
| GET | `/loans/my-loans` | Customer | Get user's loans |
| GET | `/loans/{id}` | Authenticated | Get loan details |
| GET | `/loans/all` | Officer/Admin | Get all applications |
| POST | `/loans/{id}/documents` | Customer | Upload documents |
| POST | `/loans/{id}/credit-check` | Officer | Run credit check |
| PUT | `/loans/{id}/approve` | Officer/Admin | Approve loan |
| PUT | `/loans/{id}/reject` | Officer/Admin | Reject loan |
| POST | `/loans/{id}/disburse` | Admin | Disburse loan to wallet |

### EMI (`/emi`)

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/emi/calculate` | Public | Calculate EMI preview |
| GET | `/emi/schedule/{loanId}` | Authenticated | Get EMI schedule |
| GET | `/emi/upcoming/{userId}` | Customer | Get upcoming EMIs |
| PUT | `/emi/installment/{id}/paid` | System | Mark EMI as paid |

### Wallet (`/wallet`)

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/wallet/balance` | Customer | Get wallet balance |
| GET | `/wallet/transactions` | Customer | Get transaction history |

### Payments (`/payments`)

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/payments/pay-emi` | Customer | Pay EMI installment |
| GET | `/payments/history/{loanId}` | Authenticated | Get payment history |

---

## 🗄 Database Schema

### MySQL Tables

```sql
-- lms_identity
users (id, email, password_hash, first_name, last_name, phone, role, active)

-- lms_loans  
loan_applications (id, application_number, user_id, loan_type, amount, status, ...)
application_documents (id, application_id, document_type, file_path)
user_wallet (user_id, balance, last_updated)
wallet_transactions (id, user_id, loan_id, type, amount, balance_after)

-- lms_emi
repayment_schedules (id, loan_id, user_id, installment_no, due_date, emi_amount, status, paid_date)
```

### MongoDB Collections

```javascript
// lms_notifications
notifications: { userId, type, title, message, read, sentAt }
```

---

## 🔧 Configuration

### Environment Variables (`.env`)

```properties
# Database
MYSQL_ROOT_PASSWORD=root
MYSQL_USER=lms_user
MYSQL_PASSWORD=lms_password
MONGODB_USER=mongo
MONGODB_PASS=mongo

# Messaging
RABBITMQ_USER=guest
RABBITMQ_PASS=guest

# Security
JWT_SECRET=your-256-bit-secret-key-here

# Email (Gmail App Password)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Config Server

Configurations are loaded from `config-repo/` directory:
- `application.yml` - Shared settings
- `identity-service.yml` - Identity service config
- `loan-service.yml` - Loan service config
- ... etc

---

## 🧪 Testing

```bash
# Run all unit tests
mvn test

# Run with coverage report
mvn verify

# Run specific service tests
cd identity-service && mvn test

# View JaCoCo coverage report
start target/site/jacoco/index.html
```

### Test Coverage Targets

| Service | Target Coverage |
|---------|----------------|
| Identity Service | > 80% |
| Loan Service | > 80% |
| EMI Service | > 80% |
| Payment Service | > 80% |
| Notification Service | > 80% |

---

## 🔄 CI/CD Pipeline

### Jenkins Pipeline Stages

```
┌─────────┐   ┌───────────┐   ┌────────────┐   ┌──────────────┐     ┌────────┐
│Checkout │──▶│Build/Test │──▶│ SonarCloud │──▶│ Docker Build │──▶│ Deploy │
└─────────┘   └───────────┘   └────────────┘   └──────────────┘     └────────┘
```

### Quality Gates

- ✅ All unit tests pass
- ✅ Code coverage > 80%
- ✅ No critical bugs (SonarCloud)
- ✅ No high vulnerabilities

---

## 🐛 Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| **Services not discovering each other** | Eureka not ready or services started too early | Wait for Eureka to fully start (check http://localhost:8761), then restart affected services |
| **EMI schedule not generated after disbursement** | Circuit breaker tripped due to EMI service unavailable | Check EMI service health; schedule is auto-generated via Feign call during disbursement |
| **Notifications not being sent** | RabbitMQ consumer not connected or email config missing | Verify `MAIL_USERNAME` and `MAIL_PASSWORD` in `.env`; check RabbitMQ management UI |
| **JWT authentication failing across services** | Different JWT secrets configured | Ensure `JWT_SECRET` is identical in config-repo for all services |



### Useful Commands

```bash
# Check running containers
docker compose ps

# View service logs
docker compose logs -f <service-name>

# Restart a service
docker compose restart <service-name>

# Stop all services
docker compose down

# Clean rebuild
docker compose down -v && docker compose up -d --build
```

---

## 📊 Monitoring

### Health Endpoints

All services expose Spring Actuator endpoints:

```
GET /actuator/health    # Health status
GET /actuator/info      # Application info
GET /actuator/metrics   # Metrics
```

### Eureka Dashboard

Access http://localhost:8761 to view:
- Registered services
- Instance status
- Uptime information

---

## 👩‍💻 Author

**Sravanthi Gurram**

- 🔗 GitHub: [@Sravanthi1206](https://github.com/Sravanthi1206)
- 📧 Email: sravanthigurram955@gmail.com

