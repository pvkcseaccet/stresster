# Stress Testing Tool

This repository contains a Java-based stress testing framework designed to test HTTP endpoints concurrently using `HttpsURLConnection`. It leverages `CyclicBarrier` for synchronized request execution and `FutureTask` for handling asynchronous responses. The framework is built to scale exponentially and generate performance reports.

## Features
- **Concurrent HTTP Requests**: Executes multiple HTTP requests simultaneously using a configurable number of threads.
- **Exponential Scaling**: Tests endpoints with an exponentially increasing number of requests (e.g., 2, 4, 8...).
- **Customizable Requests**: Supports JSON-defined requests with URI, method, headers, query parameters, and body.
- **Performance Reporting**: Measures and reports response times for each test iteration.
- **Form Data Support**: Sends request bodies as `application/x-www-form-urlencoded` form data.


## Prerequisites
- **Java**: JDK 11 or higher
- **Gradle**: For dependency management (optional, if using Maven)
- **JSON Library**: `org.json` for JSON parsing
- **Lombok Library**: for code generation

## Setup Steps
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/stress-tester.git
   cd stress-tester

### Prepare Test Data

- Create a `test-props.json` file in `the path of your choice` with test configurations. Example:
  ```json
  {
    "test_data_file_path": "/Your/Path/To/Test/Conf/File/test-data.json",
    "iterations":1,
    "test_type": "EXPONENTIAL",
    "domain" : "https://url.com",
    "urls_to_test":"/api/v1/anyApi"
  }
   

- Create a `test-data.json` file in `the directory as given in test-props.json file` with your request definitions. Example:
  ```json
  [{
    "request_id" : "testID",
    "data" : {
        "/api/v1/anyapi": {
            "uri": "/api/v1/anyapi",
            "http_method": "post",
            "params": {
              "user_email": "abc@xyz.com",
              "segement_id": "12264005"
            },
            "request_headers": {
              "Accept": "application/json"
            },
            "request_body": "api_data"
          }
      }
    }
  ]

### Running Stress Test with Gradle

To execute the stress testing framework using Gradle, follow these steps:


1. **Build the project**:
    ``` ./gradlew build ```
2. **Prepare the test files as shown above**
3. **Run the stress tests**:
    ``` ./gradlew run --args="--test-props=\"/Your/path/to/props/file/that/contains/testing/props\"" ```

### View Results

- Output file appears in the below directory and in console as well.
  - /test/reports/reports.txt
    ``` Tested Domain: https://url.com
        Tested API: /api/v3/anyApi
        Method: POST
        Test Mode: Concurrent+Exponential
        =============================================================================
        Total Iterations Done: 5
        Total API Calls Triggered: 32
        Total Time Spent: 49s
        =============================================================================
        
        Iteration : 1 with 2 API Calls  took 15s to execute
        Iteration : 2 with 4 API Calls  took 4s to execute
        Iteration : 3 with 8 API Calls  took 5s to execute
        Iteration : 4 with 16 API Calls  took 8s to execute
        Iteration : 5 with 32 API Calls  took 15s to execute
        =============================================================================
  ```