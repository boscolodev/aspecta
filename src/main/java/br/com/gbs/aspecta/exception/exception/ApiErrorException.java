package br.com.gbs.aspecta.exception.exception;

import br.com.gbs.aspecta.exception.ExceptionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ApiErrorException extends RuntimeException {
    private final ExceptionType type;
    private final HttpStatus status;

    public ApiErrorException(ExceptionType type, String message) {
        super(message);
        this.type = type;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ApiErrorException(ExceptionType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ApiErrorException(ExceptionType type, String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.type = type;
    }

    public ApiErrorException(ExceptionType type, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.type = type;
    }


}
