package tech.aomi.common.web.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import tech.aomi.common.web.security.AccessDeniedHandlerImpl;

/**
 * @author Sean Create At 2019/12/20
 */
public class AbstractResourceServerConfigurerAdapter extends ResourceServerConfigurerAdapter {

    @Autowired(required = false)
    private TokenStore tokenStore;

    @Autowired(required = false)
    private OAuth2ExceptionRendererImpl oAuth2ExceptionRendererImpl;

    @Autowired(required = false)
    private AccessDeniedHandlerImpl accessDeniedHandlerImpl;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        if (null != tokenStore) {
            resources.tokenStore(tokenStore);
        }
        if (null != oAuth2ExceptionRendererImpl) {
            resources.authenticationEntryPoint(oAuth2AuthenticationEntryPoint());
        }
        if (null != accessDeniedHandlerImpl) {
            resources.accessDeniedHandler(accessDeniedHandlerImpl);
        }
    }

    /**
     * 设置异常渲染服务
     */
    protected OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint() {
        OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
        oAuth2AuthenticationEntryPoint.setExceptionRenderer(oAuth2ExceptionRendererImpl);
        return oAuth2AuthenticationEntryPoint;
    }
}
