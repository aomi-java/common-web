package tech.aomi.common.web.message;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractMessageSignVerifyFilterTest {

    @Test
    public void queryStringContainsDecodedPayloadParameters() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/pay/record");
        request.setParameter("clientId", "C001");

        var wrapper = new AbstractMessageSignVerifyFilter.MessageSignVerifyRequestWrapper(
                request,
                Map.of("auditNumber", new String[]{"A 001"}),
                new byte[0]
        );

        Set<String> parameters = Stream.of(wrapper.getQueryString().split("&"))
                .collect(Collectors.toSet());

        assertEquals(Set.of("clientId=C001", "auditNumber=A+001"), parameters);
    }
}
