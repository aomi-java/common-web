package tech.aomi.common.web.message;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import tech.aomi.common.message.MessageService;
import tech.aomi.common.message.entity.BaseMessage;
import tech.aomi.common.message.entity.RequestMessage;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class AbstractMessageSignVerifyResponseBodyAdviceTest {

    @Test
    public void returnsPlainBodyWhenStatusIsNotSuccess() {
        BaseMessage body = new BaseMessage();
        body.setStatus("3001");

        TestAdvice advice = new TestAdvice();
        Object result = advice.beforeBodyWrite(
                body,
                mock(MethodParameter.class),
                MediaType.APPLICATION_JSON,
                null,
                mock(ServerHttpRequest.class),
                mock(ServerHttpResponse.class)
        );

        assertSame(body, result);
    }

    private static class TestAdvice extends AbstractMessageSignVerifyResponseBodyAdvice {

        @Override
        protected boolean supports(jakarta.servlet.http.HttpServletRequest request, MethodParameter returnType) {
            return true;
        }

        @Override
        protected MessageService getMessageService(jakarta.servlet.http.HttpServletRequest request, RequestMessage message) {
            return mock(MessageService.class);
        }
    }
}
