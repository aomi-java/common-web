package tech.aomi.common.web.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.aomi.common.constant.HttpHeader;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnClass(OncePerRequestFilter.class)
@ConditionalOnProperty(prefix = "aomi-tech.autoconfigure.web.log-id", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogIdAutoConfiguration extends OncePerRequestFilter {

    private static final String ID = "logId";

    private static final String START_AT = "START_AT";

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return new MDCTaskDecorator(ID);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isFirstRequest = !isAsyncDispatch(request);

        if (isFirstRequest) {
            long start = System.currentTimeMillis();
            String reqId = request.getHeader(HttpHeader.REQUEST_ID);
            if (null == reqId || reqId.isEmpty()) {
                reqId = UUID.randomUUID().toString().replaceAll("-", "");
            }
            MDC.put(ID, reqId);
            LOGGER.debug("请求处理开始: {}, {}", start, request.getRequestURI());
            MDC.put(START_AT, start + "");
            response.setHeader(HttpHeader.REQUEST_ID, reqId);
            request.setAttribute(HttpHeader.REQUEST_ID, reqId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            afterCompletionProcess(request);
        }
    }

    /**
     * The default value is "false" so that the filter may log a "before" message
     * at the start of request processing and an "after" message at the end from
     * when the last asynchronously dispatched thread is exiting.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    private void afterCompletionProcess(HttpServletRequest request) {
        if (isAsyncStarted(request)) {
            return;
        }

        Object reqId = request.getAttribute(HttpHeader.REQUEST_ID);
        String mdcId = MDC.get(ID);
        if (!Objects.equals(reqId, mdcId)) {
            return;
        }

        long start = 0L;
        long end = System.currentTimeMillis();
        try {
            start = Long.parseLong(MDC.get(START_AT));
        } catch (Exception ignored) {
        }
        LOGGER.debug("请求处理结束: {}, 耗时: {}, {}", end, end - start, request.getRequestURI());
        MDC.remove(START_AT);
        MDC.remove(ID);
    }
}
