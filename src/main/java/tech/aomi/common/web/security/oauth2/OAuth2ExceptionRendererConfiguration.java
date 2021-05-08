package tech.aomi.common.web.security.oauth2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.error.OAuth2ExceptionRenderer;

/**
 * @author Sean Create At 2020/1/2
 */
@Configuration
@ConditionalOnClass(OAuth2ExceptionRenderer.class)
public class OAuth2ExceptionRendererConfiguration {

    /**
     * OAuth2Exception异常处理服务
     */
    @Bean
    @ConditionalOnProperty(name = "bs.oauth2.jsonExceptionRenderer", matchIfMissing = true)
    public OAuth2ExceptionRendererImpl oAuth2ExceptionRendererImpl() {
        return new OAuth2ExceptionRendererImpl();
    }


}
