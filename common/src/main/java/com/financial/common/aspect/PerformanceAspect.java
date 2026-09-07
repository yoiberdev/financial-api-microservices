package com.financial.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    @Around("execution(* com.financial..service.*.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.nanoTime() - startTime;
            double milliseconds = executionTime / 1_000_000.0;

            // Log warning si el método tarda más de 1 segundo
            if (milliseconds > 1000) {
                log.warn("PERFORMANCE WARNING: Metodo {} tardo {} ms", methodName, String.format("%.2f", milliseconds));
            } else {
                log.debug("Performance: Metodo {} ejecutado en {} ms", methodName, String.format("%.2f", milliseconds));
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.nanoTime() - startTime;
            double milliseconds = executionTime / 1_000_000.0;
            log.error("Error en metodo {} despues de {} ms", methodName, String.format("%.2f", milliseconds));
            throw e;
        }
    }
}