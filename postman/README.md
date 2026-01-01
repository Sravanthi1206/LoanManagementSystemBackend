# LMS Postman Collection

## Files

| File | Description |
|------|-------------|
| `LMS_API_Collection.postman_collection.json` | Complete API collection (40+ endpoints) |
| `LMS_Local.postman_environment.json` | Local environment configuration |
| `run-newman-tests.bat` | Script to run tests via Newman CLI |

## Quick Start

1. **Import into Postman**:
   - Open Postman → Import → Select both JSON files

2. **Run Tests**:
   ```bash
   # Ensure backend is running first
   cd Backend
   .\start-local.bat
   
   # Run tests
   cd postman
   .\run-newman-tests.bat
   ```

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Customer | customer@lms.com | Password@123 |
| Officer | officer@lms.com | Password@123 |
| Admin | admin@lms.com | Password@123 |

## API Sections

1. **Authentication** - Register, Login (Customer/Officer/Admin)
2. **User Management** - Profile, CRUD operations
3. **Loan Application** - Apply, View, Withdraw (Customer)
4. **Loan Processing** - Review, Approve, Reject (Officer)
5. **Dashboard** - Stats, Charts, Reports
6. **EMI Service** - Calculate, Schedule, Track
7. **Wallet** - Balance, Transactions
8. **Payments** - Disburse, Repay
9. **Notifications** - View, Mark as Read

## Important Notes

- Run login requests (1.2-1.4) first to populate auth tokens
- Collection variables are automatically updated by test scripts
- Officer endpoints are under `/loans/admin/*` path
