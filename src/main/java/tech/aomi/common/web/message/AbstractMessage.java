package tech.aomi.common.web.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
@ToString
public abstract class AbstractMessage {
    /**
     * 发送请求的时间
     * 格式: yyyy-MM-dd HH:mm:ss.SSS
     */
    protected String timestamp;

    /**
     * 随机字符串
     */
    protected String randomString;

    /**
     * 报文数据.根据接口情况判断是否存在该值
     * 数据原始格式: JSON 格式字符串
     * 使用传输秘钥对原始数据(JSON 格式字符串)加密
     * Base64编码
     */
    protected String payload;

    /**
     * 请求参数编码格式
     */
    protected String charset;

    /**
     * 签名方式
     */
    protected SignType signType = SignType.RSA;

    /**
     * 请求报文签名数据 = SHA512(请求时间(timestamp) + 随机字符串(randomString) + 加密后的报文数据Base64格式(payload))
     * 响应报文签名数据 = SHA512(请求时间(timestamp) + 随机字符串(randomString) + 响应状态码 + 加密后的报文数据Base64格式(payload))
     * 报文签名、Base64编码
     * 请求时、客户端使用自身秘钥计算签名
     * 响应时、服务端使用自身秘钥计算签名
     */
    protected String sign;

    public Charset charset() {
        if (null == charset) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName(charset);
    }

}
