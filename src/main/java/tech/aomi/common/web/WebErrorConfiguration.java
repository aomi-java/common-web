package tech.aomi.common.web;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.aomi.common.web.controller.ErrorControllerImpl;

/**
 * web 错误处理bean自动配置
 * @author Sean
 */
@Configuration
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@ConditionalOnClass(ErrorController.class)
@ConditionalOnProperty(prefix = "aomi-tech.autoconfigure.web.error", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebErrorConfiguration {

    @Bean
    @ConditionalOnBean(ErrorAttributes.class)
    @ConditionalOnMissingBean(value = ErrorController.class)
    public ErrorController errorController(ErrorAttributes errorAttributes) {
        return new ErrorControllerImpl(errorAttributes, new ErrorProperties());
    }

}
