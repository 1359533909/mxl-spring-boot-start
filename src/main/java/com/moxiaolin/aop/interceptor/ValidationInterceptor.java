package com.moxiaolin.aop.interceptor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.moxiaolin.aop.annotations.Validate;
import commons.AssertUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.stereotype.Component;
import resp.IResponseStatusMsg;
import utils.CollectionUtils;
import utils.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
public class ValidationInterceptor {
    private static final Class<?>[] DEFAULT_GROUPS = new Class<?>[] {Default.class};
    private static Validator validator = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    @Around("@annotation(com.moxiaolin.aop.annotations.Validate)")
    public Object validate(ProceedingJoinPoint jp) throws Throwable {
        Object[] args = jp.getArgs();

        if (ArrayUtils.isEmpty(args)) {
            return jp.proceed(jp.getArgs());
        }
        MethodSignature sign = (MethodSignature) jp.getSignature();
        Method method = sign.getMethod();
        Validate validate = method.getAnnotation(Validate.class);
        Class<?>[] groups = ObjectUtils.defaultIfNull(validate.groups(), DEFAULT_GROUPS);
        ValidationResult result = new ValidationResult();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }

            if (arg instanceof List) {
                List<Object> argList = (List<Object>) arg;
                if (CollectionUtils.isEmpty(argList)) {
                    continue;
                }
                for (int i = 0; i < argList.size(); i++) {
                    process(argList.get(i), groups, argList.get(i).getClass().getSimpleName() + "[" + i + "].", result);
                }
            } else {
                process(arg, groups, "", result);
            }
        }

        AssertUtils.isTrue(!result.isHasErrors(), IResponseStatusMsg.APIEnum.PARAM_ERROR, result.toString());

        return jp.proceed(jp.getArgs());
    }

    private void process(Object arg, Class<?>[] groups, String desc, ValidationResult result) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(arg, groups);
        if (CollectionUtils.isEmpty(constraintViolations)) {
            return ;
        }
        result.setHasErrors(true);
        constraintViolations.forEach(cv -> {
            String errorKey = desc + cv.getPropertyPath().toString();
            if (StringUtils.isEmpty(errorKey)) {
                errorKey = desc + arg.getClass().getSimpleName();
            }
            result.addErrorMsg(errorKey, cv.getMessage());
        });
    }

    public static class ValidationResult {
        /**
         * 校验结果是否有错
         */
        private boolean hasErrors = false;

        /**
         * 校验错误信息
         */
        private Multimap<String, String> errorMsg = HashMultimap.create();

        public boolean isHasErrors() {
            return hasErrors;
        }

        public void setHasErrors(boolean hasErrors) {
            this.hasErrors = hasErrors;
        }

        public Multimap<String, String> getErrorMsg() {
            return errorMsg;
        }

        public void addErrorMsg(String key, String msg) {
            errorMsg.put(key, msg);
        }

        @Override
        public String toString() {
            if (errorMsg.isEmpty()) {
                return "{}";
            }
            Map<String, String> resultMap = Maps.newHashMap();

            errorMsg.asMap().forEach((key, values) -> {
                if (CollectionUtils.sizeOf(values) == 1) {
                    resultMap.put(key, CollectionUtils.getFirst(values));
                } else {
                    resultMap.put(key, values.toString());
                }
            });
            return resultMap.toString();
        }
    }
}

