package br.com.gbs.aspecta.logger.aspect;

import br.com.gbs.aspecta.logger.anotations.LogOn;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.interfaces.AsyncLogger;
import br.com.gbs.aspecta.logger.interfaces.I18nLogger;
import br.com.gbs.aspecta.logger.interfaces.MessageProvider;
import br.com.gbs.aspecta.logger.providers.DelegatingMessageProvider;
import br.com.gbs.aspecta.logger.service.I18NLoggerService;
import br.com.gbs.aspecta.logger.service.AsyncLoggerService;
import br.com.gbs.aspecta.logger.utils.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
@Aspect
@Component
public class LoggerAspect {

    private final LoggerProperties loggerProperties;
    private final AsyncLogger asyncLoggerService;
    private final DelegatingMessageProvider messageProvider;

    @Around("@annotation(logOn)")
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, LogOn logOn) throws Throwable {
        if (!loggerProperties.isEnabled()) return joinPoint.proceed();

        String projectName = loggerProperties.getProjectName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = targetClass.getSimpleName();
        String methodName = signature.getName();
        String argsSanitized = SensitiveDataMasker.sanitizeArgs(joinPoint.getArgs());

        asyncLoggerService.logInfo("[{}][{}] {}", projectName, className,
                messageProvider.entryMessage(methodName, argsSanitized));

        try {
            Object result = joinPoint.proceed();

            asyncLoggerService.logInfo("[{}][{}] {}", projectName, className,
                    messageProvider.exitMessage(methodName, result));

            return result;
        } catch (Throwable ex) {
            asyncLoggerService.logError("[{}][{}] {}", projectName, className,
                    messageProvider.errorMessage(methodName, ex.getClass().getSimpleName(), ex.getMessage()));
            throw ex;
        }
    }
}