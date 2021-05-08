package tech.aomi.common.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/9/5
 */
@Slf4j
public class ErrorControllerImpl extends BasicErrorController {

    private final ErrorAttributes errorAttributes;

    public ErrorControllerImpl(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = "${server.error.path:${error.path:/error}}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_HTML_VALUE})
    public ModelAndView errorException(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        WebRequest webRequest = new ServletWebRequest(request);
        Throwable t = errorAttributes.getError(webRequest);
        if (null == t) {
            return super.errorHtml(request, response);
        }
        throw t;
    }

}
