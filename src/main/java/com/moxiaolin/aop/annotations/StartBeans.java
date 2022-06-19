package com.moxiaolin.aop.annotations;

import com.moxiaolin.bean.StartBean;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author mxl
 * @version 1.0
 * @created 2021/9/20.
 * 加载启动类
 */
@Target(value = { ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(StartBean.class)
public @interface StartBeans {
}
