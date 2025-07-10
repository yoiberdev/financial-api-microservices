package com.financial.common.aspect;

import com.financial.common.annotation.Loggable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@annotation(loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String correlationId = getCurrentCorrelationId();

        // Log de entrada
        if (loggable.includeArgs()) {
            Object[] args = joinPoint.getArgs();
            log.info("[{}] Iniciando método: {} con argumentos: {}",
                    correlationId, methodName, Arrays.toString(args));
        } else {
            log.info("[{}] Iniciando método: {}", correlationId, methodName);
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            // Manejo especial para tipos reactivos
            if (result instanceof Mono) {
                return ((Mono<?>) result)
                        .doOnSuccess(data -> logSuccess(methodName, correlationId, startTime, data, loggable))
                        .doOnError(error -> logError(methodName, correlationId, startTime, error));

            } else if (result instanceof Flux) {
                return ((Flux<?>) result)
                        .doOnComplete(() -> logSuccess(methodName, correlationId, startTime, "Flux completed", loggable))
                        .doOnError(error -> logError(methodName, correlationId, startTime, error));

            } else {
                logSuccess(methodName, correlationId, startTime, result, loggable);
                return result;
            }

        } catch (Exception error) {
            logError(methodName, correlationId, startTime, error);
            throw error;
        }
    }

    private void logSuccess(String methodName, String correlationId, long startTime, Object result, Loggable loggable) {
        long executionTime = System.currentTimeMillis() - startTime;

        if (loggable.includeResult() && result != null) {
            log.info("[{}] Método {} completado en {}ms con resultado: {}",
                    correlationId, methodName, executionTime, result.toString());
        } else {
            log.info("[{}] Método {} completado exitosamente en {}ms",
                    correlationId, methodName, executionTime);
        }
    }

    private void logError(String methodName, String correlationId, long startTime, Throwable error) {
        long executionTime = System.currentTimeMillis() - startTime;
        log.error("[{}] Error en método {} después de {}ms: {}",
                correlationId, methodName, executionTime, error.getMessage(), error);
    }

    private String getCurrentCorrelationId() {
        // Implementar extracción del correlation ID del contexto actual
        return "correlation-" + Thread.currentThread().getId();
    }
}