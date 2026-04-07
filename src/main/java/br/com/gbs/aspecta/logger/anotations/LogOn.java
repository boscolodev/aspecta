package br.com.gbs.aspecta.logger.anotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LogOn {
    boolean sensitiveData() default true;
    LogLevel level() default LogLevel.INFO;
    boolean logStackTrace() default false;

    /**
     * Fields to exclude from masking for this specific method, even when
     * {@link #sensitiveData()} is {@code true} and the field name appears
     * in {@code logger.sensitive-keys}.
     * <p>
     * Example: {@code @LogOn(excludeFromMask = {"email", "name"})} will log
     * those fields in plain text while still masking everything else.
     */
    String[] excludeFromMask() default {};
}
