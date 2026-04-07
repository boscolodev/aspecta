package br.com.gbs.aspecta.exception.handler;

import br.com.gbs.aspecta.exception.dto.BasicResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@ConditionalOnClass(DataIntegrityViolationException.class)
public class DataExceptionHandler {

    private final MessageSource messageSource;

    // NOTE: The exception is intentionally not declared as a parameter.
    // DataIntegrityViolationException messages contain table names, column names,
    // and constraint details that must never be sent to the client.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        BasicResponse response = BasicResponse.builder()
                .status(status.value())
                .errorCode("DATA_INTEGRITY_VIOLATION")
                .message(resolve("exception.data.integrity", request))
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private String resolve(String key, HttpServletRequest request) {
        try {
            return messageSource.getMessage(key, null, request.getLocale());
        } catch (NoSuchMessageException e) {
            return key;
        }
    }
}
