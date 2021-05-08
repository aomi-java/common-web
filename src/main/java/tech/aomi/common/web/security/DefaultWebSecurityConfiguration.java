package tech.aomi.common.web.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Sean Create At 2019/12/20
 */
@Configuration
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
public class DefaultWebSecurityConfiguration {

    /**
     * json 处理一登录用户的403错误
     */
    @Bean
    @ConditionalOnProperty(name = "bs.security.jsonAccessDeniedHandler", matchIfMissing = true)
    public AccessDeniedHandlerImpl accessDeniedHandlerImpl() {
        return new AccessDeniedHandlerImpl();
    }

    /**
     * json 处理未登录用户的403错误
     */
    @Bean
    @ConditionalOnProperty(name = "bs.security.jsonHttp403Forbidden", matchIfMissing = true)
    public Http403ForbiddenImpl http403ForbiddenImpl() {
        return new Http403ForbiddenImpl();
    }

}
