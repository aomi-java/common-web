package tech.aomi.common.web.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import java.io.Serial;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 请求报文体
 *
 * @author Sean createAt 2021/6/22
 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class RequestMessage extends AbstractMessage implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = -6173282298420535916L;

    /**
     * 分配给客户端的唯一ID
     */
    private String clientId;

    /**
     * 使用公钥加密后的传输秘钥、Base64编码
     * 请求时,使用服务端的公钥加密
     * 响应时,使用客户端的公钥加密
     * AES 秘钥、长度128位
     */
    private String trk;

    public RequestMessage(MultiValueMap<String, String> args) {
        Optional.ofNullable(args.getFirst("charset")).ifPresent(charset -> this.charset = charset);
        Optional.ofNullable(args.getFirst("clientId")).ifPresent(clientId -> this.clientId = urlDecode(clientId));
        Optional.ofNullable(args.getFirst("trk")).ifPresent(trk -> this.trk = urlDecode(trk));
        Optional.ofNullable(args.getFirst("timestamp")).ifPresent(timestamp -> this.timestamp = urlDecode(timestamp));
        Optional.ofNullable(args.getFirst("randomString")).ifPresent(randomString -> this.randomString = urlDecode(randomString));
        Optional.ofNullable(args.getFirst("payload")).ifPresent(payload -> this.payload = urlDecode(payload));
        Optional.ofNullable(args.getFirst("signType")).ifPresent(signType -> this.signType = SignType.valueOf(signType));
        Optional.ofNullable(args.getFirst("sign")).ifPresent(sign -> this.sign = urlDecode(sign));
    }


    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, charset());
        } catch (Exception ignored) {
        }
        return value;
    }


}
