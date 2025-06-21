# Aspecta - Exception Handling & Logging Library

[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/boscolodev/aspecta/blob/main/README.md)

Aspecta is a Java library based on Spring Boot that provides a standardized framework for:

- Global exception handling with automatic responses.
- Exception factory for centralizing failures.
- AOP Logger for automatic method interception and logging.

---

## Features

### Exception Handling
- `@RestControllerAdvice` with `GlobalExceptionHandler`.
- Automatic conversion of `ApiErrorException` exceptions into standardized JSON responses (`BasicResponse` or `CompleteResponse`).
- Recursive extraction of `HttpStatus` and `ExceptionType`.

### Exception Factory
- `ApiExceptionFactory` class for fluently and centrally throwing exceptions with different overloads.

### Logging Aspect
- `@LogOn` annotation to enable automatic method logging.
- Configuration via `application.properties`.
- Logs input parameters, return values, and exceptions.

---

## Installation

**1. Add the dependency:**
If published to Maven Central or Nexus:

```xml
<dependency>
    <groupId>br.com.gbs</groupId>
    <artifactId>aspecta</artifactId>
    <version>1.0.0</version>
</dependency>
```

**2. Enable `@ComponentScan`** (if not in the same package).

---

## Configuration

In `application.properties` or `application.yml`, you can configure the logger and exception handling behavior:

```properties
# Logger Configurations
logger.enabled=true
logger.project-name=MyProject
logger.enable-i18n=true
logger.sensitive-keys=password,senha,cpf,cnpj,token

# Internationalization (i18n) configurations for log messages
# Messages are loaded from files like messages.properties, messages_en.properties, messages_pt_BR.properties
# Example of messages_en.properties:
# log.entry.message=Entering method {0} with arguments: {1}
# log.exit.message=Exiting method {0} with result: {1}
# log.error.message=Error in method {0}: {1} - {2}
```

*   `logger.enabled`: Enables or disables aspect logging (default: `true`).
*   `logger.project-name`: Defines the project name to be displayed in logs.
*   `logger.enable-i18n`: Enables or disables internationalization of log messages (default: `true`).
*   `logger.sensitive-keys`: List of keys (comma-separated) that, if found in method arguments, will have their values masked in logs (default: `password,senha,cpf,cnpj,token`).

---

## How to Use

### 1. Exception Handling

The Aspecta project provides a robust mechanism for exception handling, centered on `ApiExceptionFactory` and `GlobalExceptionHandler`.

#### Throwing Exceptions with `ApiExceptionFactory.troll()`

Use the static method `ApiExceptionFactory.troll()` to throw custom exceptions. It has overloads for different scenarios:

**1. `ApiExceptionFactory.troll(ExceptionType type, String message, Throwable cause)`**

**Usage:** Throws an `ApiErrorException` with a message, an exception type, and a cause (another exception). The default `HttpStatus` will be `INTERNAL_SERVER_ERROR`.

**Code Example:**

```java
import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.handler.ApiExceptionFactory;

public class ExampleService {
    public void processData() {
        try {
            // Simulates an operation that may fail
            throw new RuntimeException("Unexpected error during processing.");
        } catch (RuntimeException e) {
            // Throws an ApiErrorException encapsulating the original error
            throw ApiExceptionFactory.troll(ExceptionType.BASIC, "Failed to execute the operation.", e);
        }
    }
}
```

**Expected Output (HTTP Response - JSON Example):**

```json
{
  "timestamp": "2025-06-21T10:00:00.000+00:00",
  "status": 500, 
  "error": "Internal Server Error",
  "message": "Failed to execute the operation.",
  "path": "/api/example-endpoint"
}
```

**2. `ApiExceptionFactory.troll(ExceptionType type, String message, HttpStatus status)`**

**Usage:** Throws an `ApiErrorException` with a message, an exception type, and a custom `HttpStatus`.

**Code Example:**

```java
import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.handler.ApiExceptionFactory;
import org.springframework.http.HttpStatus;

public class ExampleController {
    public void validateInput(String input) {
        if (input == null || input.isEmpty()) {
            // Throws an exception with BAD_REQUEST status
            throw ApiExceptionFactory.troll(ExceptionType.BASIC, "The 'input' field cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        // ... business logic
    }
}
```

**Expected Output (HTTP Response - JSON Example):**

```json
{
  "timestamp": "2025-06-21T10:00:00.000+00:00",
  "status": 400, 
  "error": "Bad Request",
  "message": "The 'input' field cannot be empty.",
  "path": "/api/example-endpoint"
}
```

**3. `ApiExceptionFactory.troll(ExceptionType type, String message, HttpStatus status, Throwable cause)`**

**Usage:** Throws an `ApiErrorException` with a message, an exception type, a custom `HttpStatus`, and a cause.

**Code Example:**

```java
import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.handler.ApiExceptionFactory;
import org.springframework.http.HttpStatus;

public class ExampleService {
    public void accessExternalResource() {
        try {
            // Simulates a communication failure with an external service
            throw new java.io.IOException("Connection refused by external server.");
        } catch (java.io.IOException e) {
            // Throws an exception with SERVICE_UNAVAILABLE status and the original cause
            throw ApiExceptionFactory.troll(ExceptionType.COMPLETE, "Could not connect to data service.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }
}
```

**Expected Output (HTTP Response - JSON Example):**

```json
{
  "timestamp": "2025-06-21T10:00:00.000+00:00",
  "status": 503, 
  "error": "Service Unavailable",
  "message": "Could not connect to data service.",
  "path": "/api/example-endpoint"
}
```

#### Standardized Responses from `GlobalExceptionHandler`

`@RestControllerAdvice` with `GlobalExceptionHandler` automatically intercepts `ApiErrorException` and converts them into standardized JSON responses:

- **BasicResponse**: Returned for `ExceptionType.BASIC`, containing `status` and `message`.

```json
{
  "status": "404",
  "message": "User not found"
}
```

- **CompleteResponse**: Returned for `ExceptionType.COMPLETE`, containing `status`, `message`, `details`, `path`, and `timestamp`.

```json
{
  "status": "500",
  "message": "Internal Error",
  "details": "NullPointerException",
  "path": "/api/user",
  "timestamp": "2025-05-30T12:34:56"
}
```

### 2. Automatic Logging with `@LogOn`

`LoggerAspect` uses the `@LogOn` annotation to enable automatic method logging, recording input, output, and exception information. Sensitive data masking and internationalization are configurable.

#### `@LogOn` Usage Example

```java
import br.com.gbs.aspecta.logger.anotations.LogOn;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @LogOn(sensitiveData = true) // Arguments will be logged and sensitive data masked
    public String createUser(String name, String email, String password) {
        // Logic to create the user
        return "User " + name + " created successfully!";
    }

    @LogOn(sensitiveData = false) // Arguments will be logged without masking
    public String findUser(Long id) {
        // Logic to find the user
        return "User found: " + id;
    }

    @LogOn
    public void methodThatCanFail() {
        throw new IllegalStateException("Simulated error in method.");
    }
}
```

#### Log Output Examples

Log outputs are formatted according to the configurations in `application.properties` and internationalization files.

**Method Entry Log (`createUser` with `sensitiveData = true`):**

```text
[MyProject][UserService] Entering method createUser with arguments: [name=John, email=j***@example.com, password=********]
```

**Method Exit Log (`createUser` successful):**

```text
[MyProject][UserService] Exiting method createUser with result: User John created successfully!
```

**Method Error Log (`methodThatCanFail`):**

```text
[MyProject][UserService] Error in method methodThatCanFail: IllegalStateException - Simulated error in method.
```

---

## Internal Components

### Exception

- `ApiErrorException`: Standard library exception.
- `ApiExceptionFactory`: Factory for throwing exceptions (`troll`) in a centralized manner.
- `GlobalExceptionHandler`: Global handler (`@RestControllerAdvice`) for handling and formatting error responses.

### Logger

- `@LogOn`: Annotation to mark methods that should be logged.
- `LoggerAspect`: AOP Aspect that intercepts methods annotated with `@LogOn`.
- `LoggerProperties`: Configuration class that maps `application.properties` properties to the logger.

---

## Complete Example (Controller)

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @LogOn
    public User findUser(@PathVariable Long id) {
        return userService.findUser(id);
    }

    @PostMapping
    @LogOn(sensitiveData = true)
    public String createUser(@RequestBody User user) {
        return userService.createUser(user.getName(), user.getEmail(), user.getPassword());
    }
}
```

---

## Error Handling (Response Types)

| ExceptionType | Response            | Included Fields                                  |
|---------------|---------------------|---------------------------------------------------|
| `BASIC`       | `BasicResponse`     | `status`, `message`                               |
| `COMPLETE`    | `CompleteResponse`  | `status`, `message`, `details`, `path`, `timestamp` |

---

## Advantages

- Centralized error handling.
- Standardized API responses.
- Reduced boilerplate code.
- Automatic, configurable, and internationalized logging.
- Sensitive data masking in logs.

---

## Important

- The `GlobalExceptionHandler` captures `ApiErrorException` and `MethodArgumentNotValidException`.

- Logging is **highly configurable** via `application.properties` or `application.yml`.

---

## Roadmap

- [x] Support for Spring 6 `ProblemDetail`.
- [x] Support for asynchronous logs.
- [x] Internationalized error messages (i18n).
- [x] Sensitive data removal from logs.
- [x] Parameterized sensitive data removal.
- [ ] Export to Maven Central.

---

## Contributions

Pull Requests are welcome! To contribute, please follow the code guidelines and submit your PRs for review.

---

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).

---

## Contact

- Author: Guilherme Boscolo de Souza
- Email: boscolo.dev@gmail.com
- Linkedin: https://www.linkedin.com/in/guilherme-boscolo/


