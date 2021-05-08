package tech.aomi.common.web.security;//package software.sitb.common.web.security;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.AccessDecisionManager;
//import org.springframework.security.access.ConfigAttribute;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.FilterInvocation;
//import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.util.CollectionUtils;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * 权限配置资源管理器：为权限决断器提供支持，判断用户访问的资源是否在受保护的范围之内。
// * 资源权限管理器
// * 返回资源需要的权限
// *
// * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/6/7
// */
//@Slf4j
//public class FilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {
//
//    private SecurityServices securityServices;
//
//    private FilterInvocationSecurityMetadataSource superMetadataSource;
//
//    public FilterInvocationSecurityMetadataSourceImpl(SecurityServices securityServices, FilterInvocationSecurityMetadataSource superMetadataSource) {
//        this.securityServices = securityServices;
//        this.superMetadataSource = superMetadataSource;
//    }
//
//    /**
//     * 此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 {@link AccessDecisionManager#decide(Authentication, Object, Collection)} 方法，
//     * 用来判定用户是否有此权限。
//     * 如果不在权限表中则放行。
//     *
//     * @param object object
//     * @return the attributes that apply to the passed in secured object.
//     * Should return an empty collection if there are no applicable attributes.
//     */
//    @Override
//    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
//        HttpServletRequest request = ((FilterInvocation) object).getRequest();
//
//        Map<Action, Collection<ConfigAttribute>> map = securityServices.getAllResourceAttributes();
//
//        for (Action action : map.keySet()) {
//            AntPathRequestMatcher matcher = new AntPathRequestMatcher(action.getPattern(), action.getMethod());
//            if (matcher.matches(request) && !CollectionUtils.isEmpty(map.get(action))) {
//                return map.get(action);
//            }
//        }
//
//        return this.superMetadataSource.getAttributes(object);
//    }
//
//    @Override
//    public Collection<ConfigAttribute> getAllConfigAttributes() {
//        Set<ConfigAttribute> allAttributes = new HashSet<>();
//        securityServices.getAllResourceAttributes().forEach((k, v) -> allAttributes.addAll(v));
//        return allAttributes;
//    }
//
//    @Override
//    public boolean supports(Class<?> clazz) {
//        return FilterInvocation.class.isAssignableFrom(clazz);
//    }
//
//}
