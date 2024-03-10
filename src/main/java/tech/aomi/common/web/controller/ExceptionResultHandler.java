package tech.aomi.common.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tech.aomi.common.exception.ErrorCode;
import tech.aomi.common.exception.ServiceException;
import tech.aomi.common.utils.MapBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class ExceptionResultHandler {

    public static final Map<String, Function<Throwable, Result>> HANDLERS = MapBuilder.<String, Function<Throwable, Result>>builder()
            .put(IllegalArgumentException.class.getName(), ExceptionResultHandler::illegalArgumentException)
            .put(ServletRequestBindingException.class.getName(), ExceptionResultHandler::illegalArgumentException)
            .put(HttpMessageNotReadableException.class.getName(), ExceptionResultHandler::illegalArgumentException)

            .put(MethodArgumentTypeMismatchException.class.getName(), (t) -> methodArgumentTypeMismatchException((MethodArgumentTypeMismatchException) t))

            .put(MissingServletRequestParameterException.class.getName(), (t) -> missingServletRequestParameterException((MissingServletRequestParameterException) t))
            .put(BindException.class.getName(), (t) -> bindException((BindException) t))
            .put(MethodArgumentNotValidException.class.getName(), (t) -> methodArgumentNotValidException((MethodArgumentNotValidException) t))
            .put(ServiceException.class.getName(), (t) -> servicesException((ServiceException) t))

            .build();

    public static Result getResult(Throwable t) {
        Function<Throwable, Result> handler = HANDLERS.getOrDefault(t.getClass().getName(), ExceptionResultHandler::exception);
        return handler.apply(t);
    }

    public static Result illegalArgumentException(Throwable e) {
        LOGGER.error("参数错误异常: {}", e.getMessage(), e);
        return Result.create(ErrorCode.PARAMS_ERROR, e.getMessage(), null);
    }

    public static Result methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        LOGGER.error("MethodArgumentTypeMismatchException错误异常: {}", e.getMessage(), e);
        Map<String, Object> args = new HashMap<>();
        args.put("name", e.getName());
        args.put("parameter", e.getParameter());
        return Result.create(ErrorCode.PARAMS_ERROR, null, args);
    }

    public static Result missingServletRequestParameterException(MissingServletRequestParameterException e) {
        LOGGER.error("MissingServletRequestParameterException错误异常: {}", e.getMessage(), e);
        Map<String, Object> args = new HashMap<>();
        args.put("parameterName", e.getParameterName());
        args.put("parameterType", e.getParameterType());
        return Result.create(ErrorCode.PARAMS_ERROR, null, args);
    }

    public static Result bindException(BindException e) {
        LOGGER.error("BindException错误异常: {}", e.getMessage(), e);
        List<FieldError> errors = e.getFieldErrors();
        Map<String, String> errorMsg = new HashMap<>();
        errors.forEach(fieldError -> errorMsg.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return Result.create(ErrorCode.PARAMS_ERROR, null, errorMsg);
    }

    public static Result methodArgumentNotValidException(MethodArgumentNotValidException e) {
        LOGGER.error("MethodArgumentNotValidException错误异常: {}", e.getMessage(), e);
        Map<String, Object> args = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            args.put(error.getField(), error.getDefaultMessage());
        }
        return Result.create(ErrorCode.PARAMS_ERROR, null, args);
    }

    public static Result servicesException(ServiceException ex) {
        LOGGER.error("控制器发生异常: [{}]", ex.getMessage(), ex);
        return Result.create(ex.getErrorCode(), ex.getMessage(), ex.getPayload());
    }


    public static Result exception(Throwable ex) {
        if (ex instanceof ServiceException) {
            return servicesException((ServiceException) ex);
        }
        LOGGER.error("请求执行错误:{}", ex.getMessage(), ex);
        return Result.create(ErrorCode.EXCEPTION, ex.getMessage(), null);
    }

}
