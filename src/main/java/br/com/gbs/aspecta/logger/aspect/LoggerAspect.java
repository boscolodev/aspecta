package br.com.gbs.aspecta.logger.aspect;

import br.com.gbs.aspecta.logger.anotations.LogLevel;
import br.com.gbs.aspecta.logger.anotations.LogOn;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.interfaces.AsyncLogger;
import br.com.gbs.aspecta.logger.providers.DelegatingMessageProvider;
import br.com.gbs.aspecta.logger.utils.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggerAspect {

    private static final String LOG_TEMPLATE = "[{}][{}] {}";

    private final LoggerProperties loggerProperties;
    private final AsyncLogger asyncLoggerService;
    private final DelegatingMessageProvider messageProvider;
    private final SensitiveDataMasker masker;

    // ── Method-level @LogOn ───────────────────────────────────────────────────

    @Around("@annotation(logOn)")
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, LogOn logOn) throws Throwable {
        if (!loggerProperties.isEnabled()) return joinPoint.proceed();
        return doLog(joinPoint, logOn);
    }

    // ── Class-level @LogOn ────────────────────────────────────────────────────

    @Around("within(@br.com.gbs.aspecta.logger.anotations.LogOn *) "
            + "&& execution(public * *(..)) "
            + "&& !@annotation(br.com.gbs.aspecta.logger.anotations.LogOn) "
            + "&& !@annotation(br.com.gbs.aspecta.logger.anotations.LogSkip)")
    public Object logClassAnnotatedMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggerProperties.isEnabled()) return joinPoint.proceed();
        LogOn logOn = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), LogOn.class);
        if (logOn == null) return joinPoint.proceed();
        return doLog(joinPoint, logOn);
    }

    // ── Core logging logic ────────────────────────────────────────────────────

    private Object doLog(ProceedingJoinPoint joinPoint, LogOn logOn) throws Throwable {
        LogLevel level = logOn.level();
        if (!isLogLevelActive(level)) return joinPoint.proceed();

        MethodSignature sig  = (MethodSignature) joinPoint.getSignature();
        String projectName   = loggerProperties.getProjectName();
        String className     = joinPoint.getTarget().getClass().getSimpleName();
        String methodName    = sig.getName();
        List<String> exclude = Arrays.asList(logOn.excludeFromMask());

        String argsSanitized = masker.sanitizeArgs(joinPoint.getArgs(), logOn.sensitiveData(), exclude);
        emit(level, projectName, className, methodName, "entry", argsSanitized, null, -1);

        long start = System.currentTimeMillis();
        try {
            Object result   = joinPoint.proceed();
            long   duration = System.currentTimeMillis() - start;
            String resultSanitized = masker.sanitizeResult(result, logOn.sensitiveData(), exclude);
            emit(level, projectName, className, methodName, "exit", resultSanitized, null, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            // Mask the exception message before logging — it may contain sensitive field values
            String safeMessage = masker.sanitizeMessage(
                    ex.getMessage(), logOn.sensitiveData(), exclude);
            emit(level, projectName, className, methodName, "error",
                    ex.getClass().getSimpleName(), safeMessage, duration);
            if (logOn.logStackTrace()) {
                logStackTrace(level, ex);
            }
            throw ex;
        }
    }

    // ── Emit: structured vs plain ─────────────────────────────────────────────

    private void emit(LogLevel level, String project, String className, String method,
                      String event, String field1, String field2, long durationMs) {
        String traceId = MDC.get("traceId");
        if (loggerProperties.isStructuredOutput()) {
            dispatchRaw(level, buildStructuredJson(
                    event, project, className, method, field1, field2, durationMs, traceId));
        } else {
            String msg = buildPlainMessage(event, method, field1, field2, durationMs);
            String tmpl = (traceId != null && !traceId.isBlank())
                    ? "[{}][{}][traceId=" + traceId + "] {}"
                    : LOG_TEMPLATE;
            dispatchTemplate(level, tmpl, project, className, msg);
        }
    }

    private String buildPlainMessage(String event, String method,
                                     String field1, String field2, long durationMs) {
        return switch (event) {
            case "entry" -> messageProvider.entryMessage(method, field1);
            case "exit"  -> messageProvider.exitMessage(method, field1) + " | " + durationMs + "ms";
            default      -> messageProvider.errorMessage(method, field1, field2) + " | " + durationMs + "ms";
        };
    }

    private String buildStructuredJson(String event, String project, String className,
                                       String method, String field1, String field2,
                                       long durationMs, String traceId) {
        StringBuilder sb = new StringBuilder("{");
        appendStr(sb, "event",   event);
        appendStr(sb, "project", project);
        appendStr(sb, "class",   className);
        appendStr(sb, "method",  method);
        switch (event) {
            case "entry" -> appendStr(sb, "args", field1);
            case "exit"  -> { appendStr(sb, "result", field1); appendNum(sb, "durationMs", durationMs); }
            default      -> { appendStr(sb, "exception", field1);
                              appendStr(sb, "message", field2);
                              appendNum(sb, "durationMs", durationMs); }
        }
        if (traceId != null && !traceId.isBlank()) appendStr(sb, "traceId", traceId);
        sb.append("}");
        return sb.toString();
    }

    private void appendStr(StringBuilder sb, String key, String value) {
        if (sb.length() > 1) sb.append(",");
        String safe = value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
        sb.append("\"").append(key).append("\":\"").append(safe).append("\"");
    }

    private void appendNum(StringBuilder sb, String key, long value) {
        if (sb.length() > 1) sb.append(",");
        sb.append("\"").append(key).append("\":").append(value);
    }

    // ── Level dispatch ────────────────────────────────────────────────────────

    private void dispatchTemplate(LogLevel level, String tmpl,
                                  String project, String className, String message) {
        switch (level) {
            case DEBUG -> asyncLoggerService.logDebug(tmpl, project, className, message);
            case WARN  -> asyncLoggerService.logWarn(tmpl, project, className, message);
            default    -> asyncLoggerService.logInfo(tmpl, project, className, message);
        }
    }

    private void dispatchRaw(LogLevel level, String json) {
        switch (level) {
            case DEBUG -> asyncLoggerService.logDebug(json);
            case WARN  -> asyncLoggerService.logWarn(json);
            default    -> asyncLoggerService.logInfo(json);
        }
    }

    // ── logStackTrace: INFO level now correctly maps to log.info() ───────────

    void logStackTrace(LogLevel level, Throwable ex) {
        switch (level) {
            case DEBUG -> log.debug("Stack trace:", ex);
            case INFO  -> log.info("Stack trace:", ex);
            case WARN  -> log.warn("Stack trace:", ex);
        }
    }

    private boolean isLogLevelActive(LogLevel level) {
        return switch (level) {
            case DEBUG -> log.isDebugEnabled();
            case WARN  -> log.isWarnEnabled();
            default    -> log.isInfoEnabled();
        };
    }
}
