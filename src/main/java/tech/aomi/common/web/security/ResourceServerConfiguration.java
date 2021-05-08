//package tech.aomi.common.web.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.security.access.vote.AffirmativeBased;
//import org.springframework.security.config.annotation.ObjectPostProcessor;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
//import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
//import org.springframework.security.web.access.AccessDeniedHandler;
//
///**
// * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/7/12
// */
//@Configuration
//@ConditionalOnClass(ResourceServerConfigurer.class)
//public class ResourceServerConfiguration {
//
//
//    /**
//     * 资源服务器配置
//     */
//    @Configuration
//    @EnableResourceServer
//    public static class ResourceServerConfigurerImpl extends ResourceServerConfigurerAdapter {
//
//        @Autowired(required = false)
//        private AccessDeniedHandler accessDeniedHandler;
//
//        @Override
//        public void configure(HttpSecurity http) throws Exception {
//            http
//                    .sessionManagement()
//                    // 调整为让 Spring Security 不创建和使用 session
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                    .and()
//                    .authorizeRequests()
////                    .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
////                        @Override
////                        public <O extends FilterSecurityInterceptor> O postProcess(O object) {
////                            object.setSecurityMetadataSource(filterInvocationSecurityMetadataSourceImpl(object.getSecurityMetadataSource()));
////                            return object;
////                        }
////                    })
//            ;
//
//        }
//
//
//        @Override
//        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
//            if (null != accessDeniedHandler) {
//                resources.accessDeniedHandler(accessDeniedHandler);
//            }
//        }
//
//        private FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSourceImpl(FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource) {
//            if (null == securityServices)
//                return filterInvocationSecurityMetadataSource;
//            return new FilterInvocationSecurityMetadataSourceImpl(securityServices, filterInvocationSecurityMetadataSource);
//        }
//    }
//
//}
