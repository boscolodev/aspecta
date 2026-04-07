package br.com.gbs.aspecta.logger.anotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Suppresses logging for a specific method when {@link LogOn} is applied at class level.
 * Has no effect when {@link LogOn} is applied directly on the method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogSkip {
}
