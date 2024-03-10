package tech.aomi.common.web.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.Map;

/**
 * 响应报文
 *
 * @author Sean createAt 2021/6/22
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ResponseMessage extends AbstractMessage implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 5435501706637912303L;

    /**
     * 请求是否处理成功
     */
    private Boolean success;

    /**
     * 响应状态码
     */
    private String status;


    /**
     * 响应结果说明
     */
    private String describe;


    public Map<String, Object> toMap() {
        return Map.of(
                "timestamp", timestamp,
                "randomString", randomString,
                "success", success,
                "status", status,
                "payload", payload,
                "describe", describe,
                "signType", signType,
                "sign", sign
        );
    }
}
