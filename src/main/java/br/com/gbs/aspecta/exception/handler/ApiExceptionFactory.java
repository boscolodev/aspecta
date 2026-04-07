package br.com.gbs.aspecta.exception.handler;

import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.exception.ApiErrorException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiExceptionFactory {

    // ── Original overloads ────────────────────────────────────────────────────

    public static ApiErrorException troll(ExceptionType type, String message, Throwable cause) {
        throw new ApiErrorException(type, message, cause);
    }

    public static ApiErrorException troll(ExceptionType type, String message, HttpStatus status) {
        throw new ApiErrorException(type, message, status);
    }

    public static ApiErrorException troll(ExceptionType type, String message, HttpStatus status, Throwable cause) {
        throw new ApiErrorException(type, message, status, cause);
    }

    // ── Overloads with errorCode ──────────────────────────────────────────────

    public static ApiErrorException troll(ExceptionType type, String message, HttpStatus status, String errorCode) {
        throw new ApiErrorException(type, message, status, errorCode);
    }

    public static ApiErrorException troll(ExceptionType type, String message, HttpStatus status, String errorCode, Throwable cause) {
        throw new ApiErrorException(type, message, status, errorCode, cause);
    }

    // ── Overload with i18n messageKey + errorCode ─────────────────────────────

    /**
     * Throws an {@link ApiErrorException} carrying a {@code messageKey} for i18n resolution
     * and a {@code fallbackMessage} used when the key is not found.
     */
    public static ApiErrorException trollI18n(ExceptionType type, String messageKey, String fallbackMessage,
                                              HttpStatus status, String errorCode) {
        throw new ApiErrorException(type, messageKey, fallbackMessage, status, errorCode);
    }
}
