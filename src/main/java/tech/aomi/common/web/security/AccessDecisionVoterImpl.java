package tech.aomi.common.web.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/9/5
 */
@Slf4j
public class AccessDecisionVoterImpl implements AccessDecisionVoter<Object> {

    private SecurityServices securityServices;

    public AccessDecisionVoterImpl(SecurityServices securityServices) {
        this.securityServices = securityServices;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return null != attribute.getAttribute();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        if (authentication == null) {
            return ACCESS_DENIED;
        }

        try {
            HttpServletRequest request = ((FilterInvocation) object).getRequest();
            return securityServices.vote(request, authentication);
        } catch (AccessDeniedException e) {
            LOGGER.error("用户自定义授权失败: {}", e.getMessage());
        }

        int result = ACCESS_ABSTAIN;
        for (ConfigAttribute attribute : attributes) {
            if (this.supports(attribute)) {
                result = ACCESS_DENIED;
                // Attempt to find a matching granted authority
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    if (attribute.getAttribute().equals(authority.getAuthority())) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }
        return result;
    }

}
