package com.moxiaolin.aop.annotations;

import javax.validation.groups.Default;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
    /**
     * 校验分组
     * 默认：使用validation默认规则
     */
    Class<?>[] groups() default { Default.class };
}

