package com.moxiaolin.aop.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
// mvc controller异常拦截
@ControllerAdvice
@Slf4j
public class AllExceptionHandler {
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public Object handlerException(Throwable e) throws Throwable {
        throw e;
    }

}
