package br.com.gbs.aspecta.exception.handler;

import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.dto.BasicResponse;
import br.com.gbs.aspecta.exception.dto.CompleteResponse;
import br.com.gbs.aspecta.exception.exception.ApiErrorException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<?> handleApiErrorException(ApiErrorException ex, HttpServletRequest request) {
        HttpStatus status = extractStatus(ex);
        ExceptionType exceptionType = determineExceptionType(ex);

        return switch (exceptionType) {
            case BASIC -> ResponseEntity.status(status).body(buildBasicResponse(ex, status));
            case COMPLETE -> ResponseEntity.status(status).body(buildCompleteResponse(ex, request, status));
        };
    }

    private HttpStatus extractStatus(Throwable ex) {
        if (ex instanceof ApiErrorException apiEx && apiEx.getStatus() != null) {
            return apiEx.getStatus();
        }
        if (ex.getCause() != null) {
            return extractStatus(ex.getCause());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ExceptionType determineExceptionType(ApiErrorException ex) {
        if (ex.getType() != null) {
            return ex.getType();
        }
        if (ex.getCause() instanceof ApiErrorException causeEx && causeEx.getType() != null) {
            return causeEx.getType();
        }
        return ExceptionType.BASIC;
    }

    private BasicResponse buildBasicResponse(ApiErrorException ex, HttpStatus status) {
        return BasicResponse.builder()
                .status(String.valueOf(status.value()))
                .message(ex.getMessage())
                .build();
    }

    private CompleteResponse buildCompleteResponse(ApiErrorException ex, HttpServletRequest request, HttpStatus status) {
        return CompleteResponse.builder()
                .status(String.valueOf(status.value()))
                .message(ex.getMessage())
                .details(getCauseDetails(ex))
                .path(request.getRequestURI())
                .timestamp(currentTimestamp())
                .build();
    }

    private Object getCauseDetails(Throwable ex) {
        Throwable cause = ex.getCause();
        return (cause != null) ? cause.getMessage() : null;
    }

    private String currentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
