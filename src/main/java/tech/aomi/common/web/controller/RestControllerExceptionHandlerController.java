package tech.aomi.common.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tech.aomi.common.exception.ServiceException;

/**
 * Rest Controller 控制器异常处理控制器
 *
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/6/12
 */
@Slf4j
@Configuration
@RestControllerAdvice
@ConditionalOnClass(HttpServletRequest.class)
@ConditionalOnProperty(prefix = "aomi-tech.autoconfigure.web.exception", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RestControllerExceptionHandlerController {

    @ExceptionHandler({IllegalArgumentException.class, ServletRequestBindingException.class, HttpMessageNotReadableException.class})
    public Result illegalArgumentException(Exception e) {
        return ExceptionResultHandler.getResult(e);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ExceptionResultHandler.getResult(e);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result missingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ExceptionResultHandler.getResult(e);
    }

    @ExceptionHandler(BindException.class)
    public Result bindException(BindException e) {
        return ExceptionResultHandler.getResult(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ExceptionResultHandler.getResult(e);
    }

    @ExceptionHandler(ServiceException.class)
    public Result servicesException(ServiceException e) {
        return ExceptionResultHandler.getResult(e);
    }


    @ExceptionHandler
    public Result exception(Exception ex) {
        return ExceptionResultHandler.getResult(ex);
    }

}
