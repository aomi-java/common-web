package tech.aomi.common.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.*;
import java.lang.reflect.Type;

/**
 * @author Sean createAt 2018/6/12
 */
@Slf4j
public abstract class AbstractRequestSignVerifyHandler extends RequestBodyAdviceAdapter {

    public static final int EOF = -1;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected ServerProperties properties;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        EnableSignature typeEnableSignature = methodParameter.getContainingClass().getAnnotation(EnableSignature.class);
        if (null != typeEnableSignature)
            return true;
        EnableSignature enableSignature = methodParameter.getMethodAnnotation(EnableSignature.class);
        return null != enableSignature;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copy(inputMessage.getBody(), outputStream);

        byte[] body = outputStream.toByteArray();
        verify(body);

        return new HttpInputMessage() {
            @Override
            public InputStream getBody() throws IOException {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    protected abstract void verify(byte[] body);


    private void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024 * 128];

//        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
//            count += n;
        }
    }

}
