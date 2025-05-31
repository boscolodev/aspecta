# Aspecta - Exception Handling & Logging Library

[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/boscolodev/aspecta/blob/main/README-br.md)

Aspecta is a Java library based on Spring Boot that provides a standardized framework for:

✅ Global exception handling with automatic responses.  
✅ Exception factory for centralized failure management.  
✅ AOP logger for automatic method interception and logging.


---

## ✨ Features

### ✅ Exception Handling

* `@RestControllerAdvice` with `GlobalExceptionHandler`.
* Automatic conversion of `ApiErrorException` exceptions into standardized JSON responses (`BasicResponse` or `CompleteResponse`).
* Recursive extraction of `HttpStatus` and `ExceptionType`.

### ✅ Exception Factory

* `ApiExceptionFactory` class to throw exceptions fluently and centrally with different overloads.

### ✅ Logging Aspect

* `@LogOn` annotation to enable automatic method logging.
* Configuration via `application.properties`.
* Logs input parameters, return values, and exceptions.

---

## 🛠️ Installation

**1. Add the dependency:**
If published on Maven Central or Nexus:

```xml
<dependency>
    <groupId>br.com.gbs</groupId>
    <artifactId>aspecta</artifactId>
    <version>1.0.0</version>
</dependency>
```

**2. Enable `@ComponentScan`** (if not in the same package).

---

## ⚙️ Configuration

In `application.properties` or `application.yml`:

```properties
logger.enabled=true
logger.project-name=MyProject
```

---

## 📦 How to Use

### ✅ 1. Exception Handling

Throw exceptions using the `ApiExceptionFactory`:

```java
import static br.com.gbs.aspecta.exception.handler.ApiExceptionFactory.troll;

if (user == null) {
    troll(ExceptionType.BASIC, "User not found", HttpStatus.NOT_FOUND);
}
```

The `GlobalExceptionHandler` will automatically intercept and return:

* **BasicResponse**: Simple, with `status` and `message`.
* **CompleteResponse**: Detailed, with `status`, `message`, `details`, `path`, and `timestamp`.

**Example response (JSON):**

`ExceptionType.BASIC`:

```json
{
  "status": "404",
  "message": "User not found"
}
```

`ExceptionType.COMPLETE`:

```json
{
  "status": "500",
  "message": "Internal Error",
  "details": "NullPointerException",
  "path": "/api/user",
  "timestamp": "2025-05-30T12:34:56"
}
```

---

### ✅ 2. Automatic Logging

Annotate methods with `@LogOn` to enable AOP logging:

```java
import br.com.gbs.aspecta.logger.anotations.LogOn;

@Service
public class UserService {

    @LogOn
    public User findUser(Long id) {
        return repository.findById(id)
                         .orElseThrow(() -> troll(ExceptionType.BASIC, "User not found", HttpStatus.NOT_FOUND));
    }
}
```

**Example generated log:**

```text
[MyProject][UserService] Method: findUser() called | Args: [1]
[MyProject][UserService] Method: findUser() returned | Return: User{id=1, name='John'}
```

In case of an error:

```text
[MyProject][UserService] Method: findUser() threw exception | Message: User not found
```

---

## ✅ Internal Components

### 📁 Exception

* `ApiErrorException`: Default exception.
* `ApiExceptionFactory`: Factory to throw exceptions (`troll`).
* `GlobalExceptionHandler`: Global handler for response formatting and exception handling.

---

### 📁 Logger

* `@LogOn`: Annotation to mark methods to be logged.
* `LoggerAspect`: AOP aspect that intercepts methods.
* `LoggerProperties`: Configuration via `application.properties`.

---

## 📝 Complete Example

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @LogOn
    public User findUser(@PathVariable Long id) {
        return userService.findUser(id);
    }
}
```

---

## 🚨 Error Handling

| ExceptionType | Response                                      |
| ------------- | --------------------------------------------- |
| BASIC         | status + message                              |
| COMPLETE      | status + message + details + path + timestamp |

---

## ✅ Advantages

✅ Centralized error handling.
✅ Standardized API responses.
✅ Reduction of repetitive code.
✅ Automatic, configurable logging.

---

## ❗ Important

* `GlobalExceptionHandler` only catches **`ApiErrorException`**.
* To catch other exceptions, create new methods with `@ExceptionHandler`.
* Logging is **configurable** via `application.properties`.

---

## ✅ Roadmap (suggested improvements)

* [ ] Support for Spring 6’s `ProblemDetail`.
* [ ] Publish to Maven Central.
* [ ] Support asynchronous logging.
* [ ] Internationalized error messages (i18n).
* [ ] Removal of sensitive data from logs.

---

## 🤝 Contributions

Pull requests are welcome!

---

## 🛡️ License

[MIT](LICENSE)

---

## 📞 Contact

* Author: Guilherme Boscolo de Souza
* Email: [boscolo.dev@gmail.com](mailto:boscolo.dev@gmail.com)
* LinkedIn: [https://www.linkedin.com/in/guilherme-boscolo/](https://www.linkedin.com/in/guilherme-boscolo/)
