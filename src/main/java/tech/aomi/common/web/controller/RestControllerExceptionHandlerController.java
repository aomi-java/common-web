package tech.aomi.common.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tech.aomi.common.exception.ErrorCode;
import tech.aomi.common.exception.ServiceException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        LOGGER.error("参数错误异常: {}", e.getMessage(), e);
        return Result.create(ErrorCode.PARAMS_ERROR, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        LOGGER.error("MethodArgumentTypeMismatchException错误异常: {}", e.getMessage(), e);
        Map<String, Object> args = new HashMap<>();
        args.put("name", e.getName());
        args.put("parameter", e.getParameter());
        return Result.create(ErrorCode.PARAMS_ERROR, e.getMessage(), args);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result missingServletRequestParameterException(MissingServletRequestParameterException e) {
        LOGGER.error("MissingServletRequestParameterException错误异常: {}", e.getMessage(), e);
        Map<String, Object> args = new HashMap<>();
        args.put("parameterName", e.getParameterName());
        args.put("parameterType", e.getParameterType());
        return Result.create(ErrorCode.PARAMS_ERROR, e.getMessage(), args);
    }

    @ExceptionHandler(BindException.class)
    public Result bindException(BindException e) {
        LOGGER.error("BindException错误异常: {}", e.getMessage(), e);
        List<FieldError> errors = e.getFieldErrors();
        Map<String, String> errorMsg = new HashMap<>();
        errors.forEach(fieldError -> errorMsg.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return Result.create(ErrorCode.PARAMS_ERROR, e.getMessage(), errorMsg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        LOGGER.error("MethodArgumentNotValidException错误异常: {}", e.getMessage(), e);
        Map<String, Object> args = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            args.put(error.getField(), error.getDefaultMessage());
        }
        return Result.create(ErrorCode.PARAMS_ERROR, e.getMessage(), args);
    }

    @ExceptionHandler(ServiceException.class)
    public Result servicesException(ServiceException ex) {
        LOGGER.error("控制器发生异常: [{}]", ex.getMessage(), ex);
        return Result.create(ex.getErrorCode(), ex.getMessage(), ex.getPayload());
    }


    @ExceptionHandler
    public Result exception(Exception ex, HttpServletRequest request) {
        LOGGER.error("发生无法预料的错误:{}, {}", request.getRequestURL(), ex.getMessage(), ex);
        return Result.create(ErrorCode.EXCEPTION, ex.getMessage(), null);
    }

}
