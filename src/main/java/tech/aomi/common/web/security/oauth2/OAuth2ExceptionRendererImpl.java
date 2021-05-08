package tech.aomi.common.web.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.DefaultOAuth2ExceptionRenderer;
import org.springframework.web.context.request.ServletWebRequest;
import tech.aomi.common.exception.ErrorCode;
import tech.aomi.common.web.controller.Result;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/7/12
 */
@Slf4j
public class OAuth2ExceptionRendererImpl extends DefaultOAuth2ExceptionRenderer {

    @Override
    public void handleHttpEntityResponse(HttpEntity<?> responseEntity, ServletWebRequest webRequest) throws Exception {
        if (!responseEntity.hasBody()) {
            return;
        }
        OAuth2Exception ex = (OAuth2Exception) responseEntity.getBody();
        if (null == ex) {
            super.handleHttpEntityResponse(responseEntity, webRequest);
            return;
        }
        LOGGER.error("error={}, error_description={}", ex.getOAuth2ErrorCode(), ex.getMessage());
        HttpEntity<?> result = responseEntity;
        switch (ex.getOAuth2ErrorCode()) {
            case "server_error":
                break;
            case "invalid_token":
            case "unauthorized":
                result = new Result(ErrorCode.UNAUTHORIZED.getCode(), ex.getMessage(), ex.getOAuth2ErrorCode(), responseEntity.getHeaders());
                break;
            case "method_not_allowed":
                break;
            case "invalid_request":
                break;
            default:
                break;
        }
        super.handleHttpEntityResponse(result, webRequest);
    }

}
