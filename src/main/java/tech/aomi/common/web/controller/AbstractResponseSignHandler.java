package tech.aomi.common.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tech.aomi.common.constant.HttpHeader;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/6/11
 */
@Slf4j
public abstract class AbstractResponseSignHandler implements ResponseBodyAdvice<Result.Entity> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().equals(Result.class);
    }

    @Override
    public Result.Entity beforeBodyWrite(Result.Entity body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (null == body) {
            return null;
        }
        String sign = sign(body);
        response.getHeaders().add(HttpHeader.SIGNATURE, sign);
        return body;
    }

    protected abstract String sign(Result.Entity body);

}
