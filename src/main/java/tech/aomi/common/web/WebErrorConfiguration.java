package tech.aomi.common.web;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.aomi.common.web.controller.ErrorControllerImpl;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/9/5
 */
@Configuration
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@ConditionalOnProperty(prefix = "aomi-tech.autoconfigure.web.error", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebErrorConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = ErrorController.class)
    public ErrorController errorController(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        return new ErrorControllerImpl(errorAttributes, serverProperties.getError());
    }

}
