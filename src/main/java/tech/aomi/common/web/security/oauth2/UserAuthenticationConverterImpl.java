package tech.aomi.common.web.security.oauth2;

import cn.oneuser.DefaultUserDetails;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tech.aomi.common.web.security.UserDetailsService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Sean Create At 2020/1/3
 */
@Slf4j
@Setter
public class UserAuthenticationConverterImpl extends DefaultUserAuthenticationConverter {

    private static final String ID = "id";

    private Collection<? extends GrantedAuthority> defaultAuthorities = Collections.emptyList();

    private UserDetailsService userDetailsService;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> result = (Map<String, Object>) super.convertUserAuthentication(authentication);

        Object principal = authentication.getPrincipal();
        if (principal instanceof DefaultUserDetails) {
            DefaultUserDetails userDetails = (DefaultUserDetails) principal;
            result.put(ID, userDetails.getId());
        }

        return result;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        Collection<? extends GrantedAuthority> authorities = null;
        Object principal = null;

        if (map.containsKey(ID)) {
            String id = (String) map.get(ID);
            if (null != userDetailsService) {
                UserDetails user = userDetailsService.loadUserById(id, map);
                if (null != user) {
                    authorities = user.getAuthorities();
                    principal = user;
                }
            }
        }
        if (null == principal && map.containsKey(USERNAME)) {
            principal = map.get(USERNAME);
            if (userDetailsService != null) {
                UserDetails user = userDetailsService.loadUserByUsername((String) map.get(USERNAME));
                if (null != user) {
                    authorities = user.getAuthorities();
                    principal = user;
                }
            }
        }
        if (CollectionUtils.isEmpty(authorities))
            authorities = getAuthorities(map);

        return null == principal ? null : new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
        if (!map.containsKey(AUTHORITIES)) {
            return defaultAuthorities;
        }
        Object authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
                    .collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }

}
