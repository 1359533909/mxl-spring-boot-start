package com.moxiaolin.config;

import com.moxiaolin.aop.annotations.StartBeans;
import com.moxiaolin.bean.SimpleBean;
import com.moxiaolin.consts.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;

//@Configuration
//@ConditionalOnClass

/**
 *
 */
@ComponentScans(value = {
        @ComponentScan(value = "com.moxiaolin")
})
@Configuration
@PropertySources({
        @PropertySource(value = "classpath:custom.properties", ignoreResourceNotFound = true)
})
@ConditionalOnProperty(name = Constants.MXL_AUTO_CONFIG_ENABLED, matchIfMissing = true)
@StartBeans
public class MxlAutoConfiguration {

    @Bean
    public SimpleBean simpleBean(){ return new SimpleBean(); }
}
