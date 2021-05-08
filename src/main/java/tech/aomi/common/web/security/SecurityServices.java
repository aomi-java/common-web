package tech.aomi.common.web.security;

import org.springframework.security.core.Authentication;
import tech.aomi.common.exception.AccessDeniedException;

import javax.servlet.http.HttpServletRequest;

/**
 * 自定义访问策略处理
 * 安全服务接口
 *
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/7/16
 */
public interface SecurityServices {

    /**
     * 判断是否有访问权限
     *
     * @param request        http 请求信息
     * @param authentication 授权信息
     * @return 是否有权限
     * @throws AccessDeniedException 无权限时抛出该异常
     */
    default int vote(HttpServletRequest request, Authentication authentication) throws AccessDeniedException {
        throw new AccessDeniedException("用户没有实现决策方法");
    }
}
