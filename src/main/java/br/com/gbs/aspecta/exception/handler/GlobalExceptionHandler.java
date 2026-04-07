package br.com.gbs.aspecta.exception.handler;

import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.dto.BasicResponse;
import br.com.gbs.aspecta.exception.dto.CompleteResponse;
import br.com.gbs.aspecta.exception.dto.FieldMessage;
import br.com.gbs.aspecta.exception.exception.ApiErrorException;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final int MAX_CAUSE_DEPTH = 10;

    private final MessageSource messageSource;
    private final LoggerProperties loggerProperties;

    // ── Validation ────────────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex,
                                                       HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()
                ))
                .toList();

        FieldMessage response = new FieldMessage(status.value(),
                resolve("exception.validation.error", null, request));
        response.setDetails(fieldErrors);

        return ResponseEntity.status(status).body(response);
    }

    // ── ApiErrorException ─────────────────────────────────────────────────────

    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<?> handleApiErrorException(ApiErrorException ex, HttpServletRequest request) {
        HttpStatus status = extractStatus(ex);
        ExceptionType type = determineExceptionType(ex);
        String message = resolveExceptionMessage(ex, request);

        return switch (type) {
            case BASIC -> ResponseEntity.status(status).body(buildBasicResponse(ex, status, message));
            case COMPLETE -> ResponseEntity.status(status).body(buildCompleteResponse(ex, request, status, message));
            case PROBLEM_DETAIL -> ResponseEntity.status(status).body(buildProblemDetail(ex, request, status, message));
        };
    }

    // ── Common Spring exceptions ──────────────────────────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleUnreadableMessage(HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        BasicResponse response = BasicResponse.builder()
                .status(status.value())
                .errorCode("UNREADABLE_REQUEST_BODY")
                .message(resolve("exception.unreadable.message", null, request))
                .build();
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNotFound(HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        BasicResponse response = BasicResponse.builder()
                .status(status.value())
                .errorCode("HANDLER_NOT_FOUND")
                .message(resolve("exception.handler.not.found", null, request))
                .build();
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(HttpServletRequest request) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        BasicResponse response = BasicResponse.builder()
                .status(status.value())
                .errorCode("METHOD_NOT_ALLOWED")
                .message(resolve("exception.method.not.allowed", null, request))
                .build();
        return ResponseEntity.status(status).body(response);
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private BasicResponse buildBasicResponse(ApiErrorException ex, HttpStatus status, String message) {
        return BasicResponse.builder()
                .status(status.value())
                .errorCode(ex.getErrorCode())
                .message(message)
                .build();
    }

    private CompleteResponse buildCompleteResponse(ApiErrorException ex, HttpServletRequest request,
                                                   HttpStatus status, String message) {
        return CompleteResponse.builder()
                .status(status.value())
                .errorCode(ex.getErrorCode())
                .message(message)
                .details(getCauseDetails(ex))
                .path(request.getRequestURI())
                .timestamp(currentTimestamp())
                .build();
    }

    private ProblemDetail buildProblemDetail(ApiErrorException ex, HttpServletRequest request,
                                             HttpStatus status, String message) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setInstance(URI.create(request.getRequestURI()));
        if (ex.getErrorCode() != null) {
            pd.setProperty("errorCode", ex.getErrorCode());
        }
        return pd;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HttpStatus extractStatus(Throwable ex) {
        int depth = 0;
        Throwable current = ex;
        while (current != null && depth < MAX_CAUSE_DEPTH) {
            if (current instanceof ApiErrorException apiEx && apiEx.getStatus() != null) {
                return apiEx.getStatus();
            }
            current = current.getCause();
            depth++;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ExceptionType determineExceptionType(ApiErrorException ex) {
        if (ex.getType() != null) {
            return ex.getType();
        }
        if (ex.getCause() instanceof ApiErrorException cause && cause.getType() != null) {
            return cause.getType();
        }
        return ExceptionType.BASIC;
    }

    private String resolveExceptionMessage(ApiErrorException ex, HttpServletRequest request) {
        if (ex.getMessageKey() != null) {
            try {
                return messageSource.getMessage(ex.getMessageKey(), null, request.getLocale());
            } catch (NoSuchMessageException ignored) {
                // fall back to raw message
            }
        }
        return ex.getMessage();
    }

    private String resolve(String key, Object[] args, HttpServletRequest request) {
        try {
            return messageSource.getMessage(key, args, request.getLocale());
        } catch (NoSuchMessageException e) {
            return key;
        }
    }

    private Object getCauseDetails(Throwable ex) {
        if (!loggerProperties.isExposeExceptionDetails()) {
            return null;
        }
        Throwable cause = ex.getCause();
        return (cause != null) ? cause.getMessage() : null;
    }

    private String currentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
