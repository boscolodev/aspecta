package br.com.gbs.aspecta.logger.aspect;

import br.com.gbs.aspecta.logger.anotations.LogOn;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggerAspect {

    private final LoggerProperties loggerProperties;

    @Around("@annotation(logOn)")
    public Object logAnnotatedMethods(ProceedingJoinPoint joinPoint, LogOn logOn) throws Throwable {
        if (!loggerProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        String projectName = loggerProperties.getProjectName();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String className = targetClass.getSimpleName();
        String methodName = signature.getName();

        String argsString = Arrays.toString(joinPoint.getArgs());

        log.info("[{}][{}] Método: {}() com | Args: {}", projectName, className, methodName, argsString);

        try {
            Object result = joinPoint.proceed();

            log.info("[{}][{}] Método: {}() retornou | Retorno: {}", projectName, className, methodName, result);

            return result;
        } catch (Throwable ex) {
            log.error("[{}][{}] Método: {}() lançou exceção | Mensagem: {}", projectName, className, methodName, ex.getMessage(), ex);
            throw ex;
        }
    }
}
