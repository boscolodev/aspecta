package br.com.gbs.aspecta.exception.exception;

import br.com.gbs.aspecta.exception.ExceptionType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiErrorException extends RuntimeException {
    private final ExceptionType type;
    private final HttpStatus status;
    private final String errorCode;
    private final String messageKey;

    public ApiErrorException(ExceptionType type, String message) {
        super(message);
        this.type = type;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = null;
        this.messageKey = null;
    }

    public ApiErrorException(ExceptionType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.status = (cause instanceof ApiErrorException api) ? api.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = null;
        this.messageKey = null;
    }

    public ApiErrorException(ExceptionType type, String message, HttpStatus status) {
        super(message);
        this.type = type;
        this.status = status;
        this.errorCode = null;
        this.messageKey = null;
    }

    public ApiErrorException(ExceptionType type, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.status = status;
        this.errorCode = null;
        this.messageKey = null;
    }

    public ApiErrorException(ExceptionType type, String message, HttpStatus status, String errorCode) {
        super(message);
        this.type = type;
        this.status = status;
        this.errorCode = errorCode;
        this.messageKey = null;
    }

    public ApiErrorException(ExceptionType type, String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.status = status;
        this.errorCode = errorCode;
        this.messageKey = null;
    }

    public ApiErrorException(ExceptionType type, String messageKey, String fallbackMessage, HttpStatus status, String errorCode) {
        super(fallbackMessage);
        this.type = type;
        this.status = status;
        this.errorCode = errorCode;
        this.messageKey = messageKey;
    }
}
