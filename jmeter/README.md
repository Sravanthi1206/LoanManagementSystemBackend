# JMeter Performance Testing Guide

## Prerequisites
- JMeter 5.6+ installed
- Backend services running (either locally or via Docker)

## Running Tests

### Command Line (Headless)
```batch
cd Backend\jmeter

# Run tests and generate HTML report
jmeter -n -t LMS_Performance_Test.jmx -l results.jtl -e -o report

# View report: open report/index.html in browser
```

### GUI Mode (for editing)
```batch
jmeter -t LMS_Performance_Test.jmx
```

## Test Scenarios

| Test Group | Threads | Loops | Description |
|------------|---------|-------|-------------|
| Authentication | 5 | 10 | Login API tests |
| Loan Application | 10 | 5 | Apply loan, get loans |
| EMI Calculation | 20 | 20 | Calculate EMI (high load) |

## Configuration
- Edit `BASE_URL` variable for remote testing
- Edit `PORT` if API Gateway uses different port

## Interpreting Results
- **Throughput**: Requests per second
- **Error %**: Should be < 1%
- **Avg Response Time**: Should be < 500ms for APIs
