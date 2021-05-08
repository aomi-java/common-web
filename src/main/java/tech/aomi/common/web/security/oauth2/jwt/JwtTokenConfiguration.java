package tech.aomi.common.web.security.oauth2.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.security.oauth2.authserver.AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.StringUtils;

/**
 * @author Sean Create At 2020/1/2
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.security.oauth2.provider.token.TokenStore")
public class JwtTokenConfiguration {

    @Autowired(required = false)
    private ResourceServerProperties resourceServerProperties;

    @Autowired(required = false)
    private AuthorizationServerProperties authorizationServerProperties;

    @Autowired(required = false)
    private UserAuthenticationConverter userAuthenticationConverter;

    @Bean
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        String publicKey = "";
        if (null != resourceServerProperties)
            publicKey = resourceServerProperties.getJwt().getKeyValue();
        String privateKey = "";
        if (null != authorizationServerProperties)
            privateKey = authorizationServerProperties.getJwt().getKeyValue();

        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        if (!StringUtils.isEmpty(publicKey)) {
            converter.setVerifierKey(publicKey);
            converter.setVerifier(new RsaVerifier(publicKey));
        }

        if (!StringUtils.isEmpty(privateKey)) {
            converter.setSigningKey(privateKey);
        }
        DefaultAccessTokenConverter defaultAccessTokenConverter = new DefaultAccessTokenConverter();
        if (null != userAuthenticationConverter) {
            defaultAccessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
        }

        converter.setAccessTokenConverter(defaultAccessTokenConverter);
        return converter;
    }

}
