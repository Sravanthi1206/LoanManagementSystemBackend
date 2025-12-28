# Newman API Testing Guide

## Prerequisites
- Node.js installed
- Newman and HTML reporter installed:
  ```bash
  npm install -g newman newman-reporter-htmlextra
  ```

## Quick Start

### Run Tests with HTML Report
```bash
cd Backend/postman
run-newman-tests.bat
```

### Manual Command
```bash
newman run LMS_API_Collection.postman_collection.json \
  -e LMS_Local.postman_environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export reports/LMS_API_Test_Report.html
```

## Report Location
After running tests, find the HTML report at:
```
Backend/postman/reports/LMS_API_Test_Report.html
```

## Test Coverage

| Folder | Tests | Description |
|--------|-------|-------------|
| Authentication | 2 | Register, Login |
| Loan Application | 2 | Apply, Get My Loans |
| EMI Service | 1 | Calculate EMI |
| Dashboard | 3 | Stats, By Status, By Type |
| Wallet | 1 | Get Balance |
| Notifications | 1 | Get User Notifications |

**Total: 10 API Test Cases**

## Importing to Postman
1. Open Postman
2. File → Import
3. Select `LMS_API_Collection.postman_collection.json`
4. Import environment: `LMS_Local.postman_environment.json`

## CI/CD Integration
```yaml
# GitHub Actions example
- name: Run API Tests
  run: |
    npm install -g newman newman-reporter-htmlextra
    newman run postman/LMS_API_Collection.postman_collection.json \
      -e postman/LMS_Local.postman_environment.json \
      --reporters cli,junit \
      --reporter-junit-export results.xml
```
