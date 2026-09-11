package tech.aomi.common.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tech.aomi.common.exception.ErrorCode;
import tech.aomi.common.message.entity.BaseMessage;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * http请求响应结果
 *
 * @author Sean(sean.snow @ live.com) 2016/1/11
 */
public class Result extends ResponseEntity<BaseMessage> {

    private final BaseMessage entity;

    private List<Object> listMessage;

    private Map<String, Object> mapMessage;

    public Result() {
        this(HttpStatus.OK);
    }

    public Result(HttpHeaders headers) {
        this(HttpStatus.OK, headers);
    }

    public Result(HttpStatus httpStatus) {
        this(httpStatus, null);
    }

    public Result(HttpStatus httpStatus, HttpHeaders headers) {
        this(null, null, null, httpStatus, headers);
    }

    public Result(String status, String describe) {
        this(status, describe, null);
    }

    public Result(String status, String describe, Object payload) {
        this(status, describe, payload, HttpStatus.OK, null);
    }

    public Result(String status, String describe, Object payload, HttpStatus httpStatus) {
        this(status, describe, payload, httpStatus, null);
    }

    public Result(String status, String describe, Object payload, HttpHeaders headers) {
        this(status, describe, payload, HttpStatus.OK, headers);
    }

    public Result(String status, String describe, Object payload, HttpStatus httpStatus, HttpHeaders headers) {
        super(checkHeaders(headers), httpStatus);
        this.entity = new BaseMessage();
        this.setStatus(status);
        this.setDescribe(describe);
        this.add(payload);
    }

    public void setStatus(String status) {
        this.entity.setStatus(status);
        this.entity.setSuccess(status.equals(ErrorCode.SUCCESS.getCode()));
    }

    public void setDescribe(String describe) {
        this.entity.setDescribe(describe);
    }

    /**
     * 添加一个响应数据
     *
     * @param message msg
     */
    public void add(Object message) {
        if (null == this.listMessage) {
            this.listMessage = new ArrayList<>(5);
        }
        if (null != message) {
            this.listMessage.add(message);
        }
    }

    /**
     * 添加响应数据，以键值对形式
     *
     * @param key   key
     * @param value value
     */
    public void put(String key, Object value) {
        if (null == this.mapMessage) {
            this.mapMessage = new HashMap<>(5);
        }
        this.mapMessage.put(key, value);
    }

    public static Result success() {
        return success(null);
    }

    public static Result success(Object payload) {
        return create(ErrorCode.SUCCESS.getCode(), null, payload);
    }

    public static Result create(String status) {
        return create(status, null);
    }

    public static Result create(String status, String describe) {
        return create(status, describe, null);
    }

    public static Result create(String status, String describe, Object payload) {
        return new Result(status, describe, payload);
    }

    public static Result create(Serializable status, String describe, Object payload) {
        if (status instanceof ErrorCode) {
            return create(((ErrorCode) status).getCode(), describe, payload);
        }
        if (status instanceof Enum) {
            try {
                Method method = status.getClass().getMethod("getCode");
                Object code = method.invoke(status);
                return create(code.toString(), describe, payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return create(status.toString(), describe, payload);
    }

    @Override
    public BaseMessage getBody() {
        if (null == this.listMessage && null == this.mapMessage) {
            return this.entity;
        }
        ArrayList<Object> result = new ArrayList<>(3);

        if (null != this.listMessage && !this.listMessage.isEmpty()) {
            result.addAll(this.listMessage);
        }

        if (null != this.mapMessage && !this.mapMessage.isEmpty()) {
            result.add(this.mapMessage);
        }
        if (result.isEmpty()) {
            this.entity.setPayload(null);
        } else {
            this.entity.setPayload(result.size() == 1 ? result.get(0) : result);
        }
        return this.entity;
    }

    @Override
    public boolean hasBody() {
        return null != this.listMessage && !this.listMessage.isEmpty() || null != this.mapMessage && !this.mapMessage.isEmpty();
    }

    private static HttpHeaders checkHeaders(HttpHeaders headers) {
        HttpHeaders result = new HttpHeaders();
        result.putAll(Optional.ofNullable(headers).orElse(new HttpHeaders()));
        result.setContentType(MediaType.APPLICATION_JSON);
        return result;
    }

}
