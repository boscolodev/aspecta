package br.com.gbs.aspecta.logger.aspect;

import br.com.gbs.aspecta.logger.anotations.LogOn;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.i18n.I18nService;
import br.com.gbs.aspecta.logger.service.AsyncLoggerService;
import br.com.gbs.aspecta.logger.utils.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggerAspect {

    private final LoggerProperties loggerProperties;
    private final AsyncLoggerService asyncLoggerService;
    private final I18nService i18nService;

    @Around("@annotation(logOn)")
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, LogOn logOn) throws Throwable {
        if (!loggerProperties.isEnabled()) return joinPoint.proceed();

        String projectName = loggerProperties.getProjectName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = targetClass.getSimpleName();
        String methodName = signature.getName();

        // Saneamento de dados sensíveis
        String argsSanitized = SensitiveDataMasker.sanitizeArgs(joinPoint.getArgs());

        asyncLoggerService.logInfo("[{}][{}] Entrando no método: {}() com | Args: {}",
                projectName, className, methodName, argsSanitized);

        try {
            Object result = joinPoint.proceed();

            asyncLoggerService.logInfo("[{}][{}] Saindo do método: {}() retornou | Retorno: {}",
                    projectName, className, methodName, result);

            return result;

        } catch (Throwable ex) {
            String errorMsg = i18nService.getMessage("log.error",
                    new Object[]{methodName, ex.getClass().getSimpleName(), ex.getMessage()},
                    Locale.getDefault());

            asyncLoggerService.logError("[{}][{}] {}", projectName, className, errorMsg);
            throw ex;
        }
    }
}
