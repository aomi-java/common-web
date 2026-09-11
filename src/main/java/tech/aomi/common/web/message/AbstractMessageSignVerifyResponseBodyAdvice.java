package tech.aomi.common.web.message;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tech.aomi.common.message.MessageService;
import tech.aomi.common.message.entity.BaseMessage;
import tech.aomi.common.message.entity.MessageContent;
import tech.aomi.common.message.entity.RequestMessage;
import tech.aomi.common.web.controller.Result;

/**
 * 报文响应签名公共逻辑
 */
public abstract class AbstractMessageSignVerifyResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        HttpServletRequest request = currentRequest();
        return request != null && supports(request, returnType);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {
        if (!selectedContentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return body;
        }
        if (!(body instanceof BaseMessage entity)) {
            return body;
        }

        HttpServletRequest servletRequest = currentRequest();
        if (servletRequest == null) {
            return body;
        }
        MessageContent content = (MessageContent) servletRequest.getAttribute(MessageContent.MESSAGE_CONTEXT);
        if (content == null) {
            return body;
        }

        MessageService messageService = getMessageService(servletRequest, content.getRequestMessage());
        messageService.createResponse(content, entity.getStatus(), entity.getDescribe(), entity.getPayload());
        return content.getResponseMessage();
    }

    protected abstract boolean supports(HttpServletRequest request, MethodParameter returnType);

    protected abstract MessageService getMessageService(HttpServletRequest request, RequestMessage message);

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
