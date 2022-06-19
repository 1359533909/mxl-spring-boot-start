package com.moxiaolin.aop.interceptor;

import com.moxiaolin.aop.annotations.ExHandler;
import exception.APIRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

@Aspect
@Component
@Slf4j
public class ExInterceptor {
    /**
     * 所有标有ExHandler注解的bean
     */
    @Pointcut("within(@com.moxiaolin.aop.annotations.ExHandler *)")
    public void cutBeanAnnotatedWithTransaction(){
    }

    /**
     * 所有公共方法
     */
    @Pointcut("execution(public * *(..))")
    public void cutAllPublicMethod(){
    }

    /**
     * 标有ExHandler注解的方法
     */
    @Pointcut("@annotation(com.moxiaolin.aop.annotations.ExHandler)")
    public void cutAnnotation(){

    }

    /**
     * 所有标有注解bean下的所有方法拦截
     */
    @Around("cutBeanAnnotatedWithTransaction() && cutAllPublicMethod()")
    public Object aroundAll(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return proceedWithTransaction(joinPoint, target, method);
    }

    /**
     * 拦截标有注解的方法
     */
    @Around("cutAnnotation()")
    public Object aroundAnnotated(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object result = null;
        if (method.isAnnotationPresent(ExHandler.class)){
            result = proceedWithTransaction(joinPoint, target, method);
        }else {
            log.error("ExHandler unexpect location");
        }
        return result;
    }

    private Object proceedWithTransaction(ProceedingJoinPoint joinPoint, Object target, Method method) throws Throwable {
        String targetName = target.getClass().getName();
        String methodName = method.getName();
        try {
            return joinPoint.proceed();
        } catch (APIRuntimeException e) {
            // 自定义异常
            // 构造日志中心
            // 原始异常的 message
            String message = e.getMessage();
            // [1000,1999] 不进行封装处理，打印error之后直接抛出:属于严重的代码异常或者机器异常
            if (e.getCode() != null && e.getCode() >= 1000 && e.getCode() <2000){
                log.error("methodName:{}, e ->", methodName,e);
                throw e;
            }else {
                // 打印warn日志
                log.warn("methodName{}, e ->",methodName,e);
                return buildReturnObject(method, e);
            }
        }catch (Exception e){
            // 其他异常
            Throwable realE = e;
            // 解决动态代理等情况下导致的异常类型丢失问题
            while (realE instanceof UndeclaredThrowableException){
                realE = ((UndeclaredThrowableException) realE).getUndeclaredThrowable();
            }
            // 打印日志信息
            log.error("methodName{}, e ->",methodName,e);
            throw  realE;
        }
    }

    private Object buildReturnObject(Method method, APIRuntimeException e) {
        try{
            Class<?> returnType = method.getReturnType();
            if (returnType != Void.TYPE){
                // 存在问题，若无默认无参构造方法，将会抛出异常
                Constructor<?> constructor = returnType.getConstructor();
                Object result = constructor.newInstance();
                Method setStatus = ReflectionUtils.findMethod(returnType, "setStatus", Integer.class);
                Method setMessage = ReflectionUtils.findMethod(returnType, "setMessage", String.class);
                if (setStatus != null){
                    ReflectionUtils.invokeMethod(setStatus, result, e.getCode());
                }
                if (setMessage != null){
                    ReflectionUtils.invokeMethod(setMessage, result, e.getMessage());
                }
                return result;
            }
            return null;
        }catch (Exception ex){
            log.warn("buildReturnObject fail, e ->",ex);
            throw e;
        }
    }
}
