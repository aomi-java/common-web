package tech.aomi.common.web.message;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tech.aomi.common.exception.ErrorCode;
import tech.aomi.common.exception.ServiceException;
import tech.aomi.common.exception.SignatureException;
import tech.aomi.common.utils.crypto.AesUtils;
import tech.aomi.common.utils.crypto.RSAUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 报文签名、验签过滤器
 */
@Slf4j
@Getter
@AllArgsConstructor
public abstract class AbstractMessageSignVerifyFilter extends OncePerRequestFilter {

    private String signAlgorithm;
    private String aesTransformation;
    private int aesKeyLength;


    public AbstractMessageSignVerifyFilter() {
        this.signAlgorithm = RSAUtil.SIGN_ALGORITHMS_SHA512;
        this.aesTransformation = AesUtils.AES_CBC_PKCS5Padding;
        this.aesKeyLength = 128;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);


        MessageContent<?> content;
        if ("get".equalsIgnoreCase(request.getMethod())) {
            MultiValueMap<String, String> data = new MultiValueMapAdapter<>(new HashMap<>());
            request.getParameterMap().forEach((k, v) -> data.put(k, List.of(v)));
            RequestMessage message = new RequestMessage(data);
            content = toMessageContent(message);
        } else {
            byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
            content = toMessageContent(requestBody);
        }
        verify(content);
        byte[] newBody = payloadPlaintext(content);

        try {
            filterChain.doFilter(new MessageSignVerifyRequestWrapper(request, newBody), responseWrapper);
            byte[] responseBody = responseWrapper.getContentAsByteArray();

            ResponseMessage message = new ResponseMessage();
            message.setTimestamp(timestamp());
            message.setRandomString(UUID.randomUUID().toString());
            message.setCharset(content.getRequestMessage().getCharset());
            message.setSignType(content.getRequestMessage().getSignType());

            content.setResponseMessage(message);
            content.setResponsePayload(responseBody);

            responseHandler(content);

            byte[] payload = aes(true, content.getTrk(), content.getResponsePayload());
            String payloadStr = Base64.getEncoder().encodeToString(payload);
            message.setPayload(payloadStr);

            String sign = rsaSign(content.getPrivateKey(), getSignData(message));
            message.setSign(sign);

            responseWrapper.resetBuffer();
            responseWrapper.getOutputStream().write(toBytes(message));

        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }


    protected abstract MessageContent<?> toMessageContent(RequestMessage message);

    protected abstract MessageContent<?> toMessageContent(byte[] body);

    /**
     * 对responseBody进行预处理。
     * 更新response的status\describe\值
     * 更新content#responsePayload
     */
    protected abstract void responseHandler(MessageContent<?> content);

    protected abstract String timestamp();

    protected abstract byte[] toBytes(ResponseMessage message);

    protected void verify(MessageContent<?> content) throws ServiceException {
        RequestMessage body = content.getRequestMessage();
        LOGGER.debug("请求参数签名验证: {}", body);
        byte[] signData = getSignData(body);

        boolean isOk = false;
        switch (body.getSignType()) {
            case RSA:
                isOk = rsaVerify(content.getClientPublicKey(), signData, body.getSign());
                break;
        }
        if (isOk) {
            return;
        }
        LOGGER.error("签名校验失败: {}", body.getSign());
        throw new SignatureException("签名校验失败");
    }

    protected byte[] getSignData(RequestMessage body) {
        String signData = body.getTimestamp() + body.getRandomString() + body.getPayload();
        LOGGER.debug("请求签名数据: [{}]", signData);
        try {
            return DigestUtils.sha512(signData.getBytes(body.getCharset()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("不支持的编码:" + body.getCharset(), e);
        }
    }

    protected byte[] getSignData(ResponseMessage body) {
        String signData = body.getTimestamp() + body.getRandomString() + body.getStatus() + body.getPayload();
        LOGGER.debug("响应签名数据: [{}]", signData);
        try {
            return DigestUtils.sha512(signData.getBytes(body.getCharset()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("不支持的编码:" + body.getCharset(), e);
        }
    }

    /**
     * RSA 签名验证
     *
     * @return 签名是否正确
     */
    protected boolean rsaVerify(PublicKey publicKey, byte[] signData, String sign) {
//        PublicKey publicKey;
//        try {
//            publicKey = RSA.parsePublicKeyWithBase64(publicKeyStr);
//        } catch (Exception e) {
//            LOGGER.error("公钥格式不正确,无法解析:" + e.getMessage(), e);
//            return false;
//        }

        LOGGER.debug("待验证的签名Base64: [{}]", sign);
        try {
            byte[] signBytes = Base64.getDecoder().decode(sign);
            return RSAUtil.signVerify(
                    publicKey,
                    this.signAlgorithm,
                    signData,
                    signBytes
            );
        } catch (Exception e) {
            LOGGER.error("签名验证执行失败: {}", e.getMessage(), e);
        }
        return false;
    }

    protected String rsaSign(PrivateKey privateKey, byte[] signData) {
//        PrivateKey privateKey;
//        try {
//            privateKey = RSA.parsePrivateKeyWithBase64(key);
//        } catch (Exception e) {
//            LOGGER.error("解析私钥失败: {}", e.getMessage(), e);
//            throw new ServiceException("解析私钥失败", e);
//        }

        try {
            byte[] signArr = RSAUtil.sign(
                    privateKey,
                    this.signAlgorithm,
                    signData
            );
            return Base64.getEncoder().encodeToString(signArr);
        } catch (Exception e) {
            LOGGER.error("响应参数计算签名失败: {}", e.getMessage(), e);
            throw new ServiceException("响应参数计算签名失败", e);
        }
    }


    /**
     * @param content 请求上下文内容
     */
    protected byte[] payloadPlaintext(MessageContent<?> content) {
        RequestMessage message = content.getRequestMessage();
        LOGGER.debug("解密传输秘钥: [{}]", message.getTrk());
        byte[] trk;
        try {
            trk = RSAUtil.privateKeyDecrypt(content.getPrivateKey(), Base64.getDecoder().decode(message.getTrk()));
            content.setTrk(trk);
        } catch (Exception e) {
            LOGGER.error("解密传输秘钥失败: {}", e.getMessage(), e);
            ServiceException se = new ServiceException("使用服务端私钥解密传输秘钥失败", e);
            se.setErrorCode(ErrorCode.PARAMS_ERROR);
            throw se;
        }

        String payloadCiphertext = message.getPayload();
        LOGGER.debug("解密请求参数: [{}]", payloadCiphertext);
        if (!StringUtils.hasLength(payloadCiphertext)) {
            LOGGER.info("明文为空,不需要解密");
            return new byte[0];
        }
        byte[] payload = aes(false, trk, Base64.getDecoder().decode(payloadCiphertext));
        LOGGER.debug("请求参数明文: [{}]", new String(payload, StandardCharsets.UTF_8));
        content.setRequestPayload(payload);
        return payload;
    }


    private byte[] aes(boolean encrypt, byte[] key, byte[] data) {
        AesUtils.setTransformation(this.aesTransformation);
        AesUtils.setKeyLength(this.aesKeyLength);
        try {
            if (encrypt) {
                return AesUtils.encrypt(key, data);
            } else {
                return AesUtils.decrypt(key, data);
            }
        } catch (Exception e) {
            LOGGER.error("使用传输秘钥加解密失败: {}", e.getMessage());
            ServiceException se = new ServiceException("使用传输秘钥加解密失败", e);
            se.setErrorCode(ErrorCode.PARAMS_ERROR);
            throw se;
        }
    }


    public static class MessageSignVerifyRequestWrapper extends HttpServletRequestWrapper {


        private final ByteArrayInputStream inputStream;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public MessageSignVerifyRequestWrapper(HttpServletRequest request, byte[] body) {
            super(request);
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {

            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                }

                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }
            };
        }
    }

}
