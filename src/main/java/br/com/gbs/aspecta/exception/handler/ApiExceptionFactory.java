package br.com.gbs.aspecta.exception.handler;


import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.exception.ApiErrorException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiExceptionFactory {


    public static ApiErrorException troll(ExceptionType type, String message, Throwable cause) {
        throw new ApiErrorException(type, message, cause);
    }

    public static ApiErrorException troll(ExceptionType type, String message, HttpStatus status) {
        throw new ApiErrorException(type, message, status);
    }

    public static ApiErrorException troll(ExceptionType type, String message, HttpStatus status, Throwable cause) {
        throw new ApiErrorException(type, message, status, cause);
    }
}
