package tech.aomi.common.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import tech.aomi.common.exception.ErrorCode;
import tech.aomi.common.web.controller.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 403 处理器
 * 处理无权限结果
 *
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/7/10
 */
public class Http403ForbiddenImpl extends Http403ForbiddenEntryPoint {

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2) throws IOException {
        Result result = new Result(ErrorCode.UNAUTHORIZED.getCode(), arg2.getMessage());
        mappingJackson2HttpMessageConverter.write(result.getBody(), MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }

}
