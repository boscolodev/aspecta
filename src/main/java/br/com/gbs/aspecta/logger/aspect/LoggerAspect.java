package br.com.gbs.aspecta.logger.aspect;

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
import org.springframework.stereotype.Component;

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

    @Around("@annotation(logOn)")
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, LogOn logOn) throws Throwable {
        if (!loggerProperties.isEnabled()) return joinPoint.proceed();

        String projectName = loggerProperties.getProjectName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = targetClass.getSimpleName();
        String methodName = signature.getName();

        String argsSanitized = masker.sanitizeArgs(joinPoint.getArgs(), logOn.sensitiveData());

        asyncLoggerService.logInfo(LOG_TEMPLATE, projectName, className,
                messageProvider.entryMessage(methodName, argsSanitized));

        try {
            Object result = joinPoint.proceed();

            asyncLoggerService.logInfo(LOG_TEMPLATE, projectName, className,
                    messageProvider.exitMessage(methodName, result));

            return result;
        } catch (Throwable ex) {
            asyncLoggerService.logError(LOG_TEMPLATE, projectName, className,
                    messageProvider.errorMessage(methodName, ex.getClass().getSimpleName(), ex.getMessage()));
            throw ex;
        }
    }
}
