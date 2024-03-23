package tech.aomi.common.web.message;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import tech.aomi.common.message.MessageEncodeDecodeService;
import tech.aomi.common.message.MessageService;
import tech.aomi.common.message.entity.MessageContent;
import tech.aomi.common.message.entity.RequestMessage;
import tech.aomi.common.web.controller.ExceptionResultHandler;
import tech.aomi.common.web.controller.Result;
import tech.aomi.common.web.controller.Result.Entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 报文签名、验签过滤器
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractMessageSignVerifyFilter extends OncePerRequestFilter {

    private MessageEncodeDecodeService messageEncodeDecodeService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        MessageService messageService;
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            MessageContent content;
            if ("get".equalsIgnoreCase(request.getMethod())) {
                Map<String, String> data = new HashMap<>();
                while (request.getParameterNames().hasMoreElements()) {
                    String name = request.getParameterNames().nextElement();
                    String value = request.getParameter(name);
                    data.put(name, value);
                }
                var message = new RequestMessage(data);
                messageService = this.getMessageService(request, message);

                content = messageService.parse(message);
            } else {
                byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
                var message = this.messageEncodeDecodeService.byte2Message(requestBody, RequestMessage.class);
                messageService = this.getMessageService(request, message);
                content = messageService.parse(message);
            }
            request.setAttribute(MessageContent.MESSAGE_CONTEXT, content);
            byte[] newBody = content.getRequestPayload();

            Map<String, String[]> modifiableParameters = null;
            if ("get".equalsIgnoreCase(request.getMethod())) {
                Map<String, String> urlArgs = messageEncodeDecodeService.byte2Message(newBody, HashMap.class);
                modifiableParameters = new HashMap<>();
                for (String key : urlArgs.keySet()) {
                    modifiableParameters.put(key, new String[] { urlArgs.get(key) });
                }
            }

            filterChain.doFilter(new MessageSignVerifyRequestWrapper(request, modifiableParameters, newBody),
                    responseWrapper);
            byte[] responseBody = responseWrapper.getContentAsByteArray();
            Entity entity = messageEncodeDecodeService.byte2Message(responseBody, Result.Entity.class);

            messageService.createResponse(content, entity.getStatus(), entity.getDescribe(), entity.getPayload());

            byte[] newResponseBody = messageEncodeDecodeService.message2Byte(content.getResponseMessage());

            responseWrapper.resetBuffer();
            responseWrapper.getOutputStream().write(newResponseBody);

        } catch (Exception ex) {
            var result = ExceptionResultHandler.getResult(ex);
            responseWrapper.resetBuffer();
            responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
            responseWrapper.getOutputStream().write(messageEncodeDecodeService.message2Byte(result.getBody()));

        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    protected abstract MessageService getMessageService(HttpServletRequest request, RequestMessage message);

    public static class MessageSignVerifyRequestWrapper extends HttpServletRequestWrapper {

        private final ByteArrayInputStream inputStream;
        private final Map<String, String[]> modifiableParameters;
        private Map<String, String[]> allParameters = null;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public MessageSignVerifyRequestWrapper(HttpServletRequest request, Map<String, String[]> modifiableParameters,
                byte[] body) {
            super(request);
            this.modifiableParameters = Optional.ofNullable(modifiableParameters).orElse(new HashMap<>());
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {

            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }

                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }
            };
        }

        @Override
        public String getParameter(String name) {
            String[] params = getParameterValues(name);
            if (params != null && params.length > 0) {
                return params[0];
            }
            return super.getParameter(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if (allParameters == null) {
                allParameters = new HashMap<>();
                allParameters.putAll(super.getParameterMap());
                allParameters.putAll(modifiableParameters);
            }
            return Collections.unmodifiableMap(allParameters);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(getParameterMap().keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            if (modifiableParameters.containsKey(name)) {
                return modifiableParameters.get(name);
            }
            return super.getParameterValues(name);
        }
    }

}
