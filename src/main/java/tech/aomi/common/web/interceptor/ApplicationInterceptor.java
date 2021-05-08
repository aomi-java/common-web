package tech.aomi.common.web.interceptor;

import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 注解拦截器实现和总拦截器功能
 *
 * @author Sean sean.snow@live.com
 */
public class ApplicationInterceptor extends HandlerInterceptorAdapter {

    private final ApplicationContext applicationContext;

    public ApplicationInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return super.preHandle(request, response, handler);
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> handlerType = handlerMethod.getBeanType();

        With with = handlerType.getAnnotation(With.class);
        if (preHandlerProcess(with, request, response, handler)) {

            Method handlerBeanMethod = handlerMethod.getMethod();

            with = handlerBeanMethod.getAnnotation(With.class);

            return preHandlerProcess(with, request, response, handler);
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> handlerType = handlerMethod.getBeanType();
        With with = handlerType.getAnnotation(With.class);
        postHandlerProcess(with, request, response, handler, modelAndView);
        with = handlerMethod.getMethod().getAnnotation(With.class);
        postHandlerProcess(with, request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> handlerType = handlerMethod.getBeanType();
        With with = handlerType.getAnnotation(With.class);
        afterCompletionProcess(with, request, response, handler, ex);
        with = handlerMethod.getMethod().getAnnotation(With.class);
        afterCompletionProcess(with, request, response, handler, ex);
    }

    private void afterCompletionProcess(With with, HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (with != null) {
            Class<?>[] handlerInterceptors = with.value();
            for (Class<?> handlerInterceptor : handlerInterceptors) {
                HandlerInterceptor tmp = (HandlerInterceptor) this.applicationContext.getBean(handlerInterceptor);
                tmp.afterCompletion(request, response, handler, ex);
            }
        }
    }

    private void postHandlerProcess(With with, HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (with != null) {
            Class<?>[] handlerInterceptors = with.value();
            for (Class<?> handlerInterceptor : handlerInterceptors) {
                HandlerInterceptor tmp = (HandlerInterceptor) this.applicationContext.getBean(handlerInterceptor);
                tmp.postHandle(request, response, handler, modelAndView);
            }
        }
    }


    private boolean preHandlerProcess(With with, HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (null != with) {
            Class<?>[] handlerInterceptors = with.value();

            for (Class<?> handlerInterceptor : handlerInterceptors) {
                HandlerInterceptor tmp = (HandlerInterceptor) this.applicationContext.getBean(handlerInterceptor);
                boolean tmpResult = tmp.preHandle(request, response, handler);
                if (!tmpResult) {
                    return false;
                }
            }
        }
        return true;
    }
}
