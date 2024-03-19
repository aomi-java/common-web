package tech.aomi.common.web.message;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import tech.aomi.common.exception.ErrorCode;
import tech.aomi.common.exception.ServiceException;
import tech.aomi.common.exception.SignatureException;
import tech.aomi.common.utils.crypto.AesUtils;
import tech.aomi.common.utils.crypto.RSAUtil;
import tech.aomi.common.utils.json.Json;
import tech.aomi.common.web.controller.ExceptionResultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

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
        try {
            MessageContent content;
            if ("get".equalsIgnoreCase(request.getMethod())) {
                MultiValueMap<String, String> data = new MultiValueMapAdapter<>(new HashMap<>());
                request.getParameterMap().forEach((k, v) -> data.put(k, List.of(v)));
                RequestMessage message = new RequestMessage(data);
                content = toMessageContent(request, message);
            } else {
                byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
                content = toMessageContent(request, requestBody);
            }
            verify(content);
            byte[] newBody = payloadPlaintext(content);


            Map<String, String[]> modifiableParameters = null;
            if ("get".equalsIgnoreCase(request.getMethod())) {
                String newBodyStr = new String(newBody, content.getRequestMessage().charset());
                Map<String, String> urlArgs = Json.fromJson(newBodyStr, new TypeReference<>() {
                });
                modifiableParameters = new HashMap<>();
                for (String key : urlArgs.keySet()) {
                    modifiableParameters.put(key, new String[]{urlArgs.get(key)});
                }
            }

            filterChain.doFilter(new MessageSignVerifyRequestWrapper(request, modifiableParameters, newBody), responseWrapper);
            byte[] responseBody = responseWrapper.getContentAsByteArray();

            ResponseMessage message = new ResponseMessage();
            content.setResponseMessage(message);
            content.setResponsePayload(responseBody);

            responseHandler(content);
            message.setRandomString(UUID.randomUUID().toString());
            message.setCharset(content.getRequestMessage().getCharset());
            message.setSignType(content.getRequestMessage().getSignType());

            if (null != content.getResponsePayload() && content.getResponsePayload().length > 0) {
                byte[] payload = aes(true, content.getTrk(), content.getResponsePayload());
                String payloadStr = Base64.getEncoder().encodeToString(payload);
                message.setPayload(payloadStr);
            }

            String sign = rsaSign(content.getPrivateKey(), getSignData(message));
            message.setSign(sign);

            responseWrapper.resetBuffer();
            responseWrapper.getOutputStream().write(toBytes(message));

        } catch (Exception ex) {
            var result = ExceptionResultHandler.getResult(ex);
            responseWrapper.resetBuffer();
            responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
            responseWrapper.getOutputStream().write(toBytes(result.getBody()));
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }


    protected abstract MessageContent toMessageContent(HttpServletRequest request, RequestMessage message);

    protected abstract MessageContent toMessageContent(HttpServletRequest request, byte[] body);

    /**
     * 对responseBody进行预处理。
     * 更新response的status\describe\timespace\值
     * 更新content#responsePayload
     */
    protected abstract void responseHandler(MessageContent content);

    protected abstract byte[] toBytes(Object message);

    protected void verify(MessageContent content) throws ServiceException {
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
        String signData = body.getTimestamp() + body.getRandomString() + Optional.ofNullable(body.getPayload()).orElse("");
        LOGGER.debug("请求签名数据: [{}]", signData);
        try {
            byte[] data = DigestUtils.sha512(signData.getBytes(body.getCharset()));
            LOGGER.debug("请求签名数据SHA512: [{}]", Base64.getEncoder().encodeToString(data));
            return data;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("不支持的编码:" + body.getCharset(), e);
        }
    }

    protected byte[] getSignData(ResponseMessage body) {
        String signData = body.getTimestamp() + body.getRandomString() + body.getStatus() + Optional.ofNullable(body.getPayload()).orElse("");
        LOGGER.debug("响应签名数据: [{}]", signData);
        try {
            byte[] data = DigestUtils.sha512(signData.getBytes(body.getCharset()));
            LOGGER.debug("响应签名数据SHA512: [{}]", Base64.getEncoder().encodeToString(data));
            return data;
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
    protected byte[] payloadPlaintext(MessageContent content) {
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
        LOGGER.debug("请求参数明文: [{}]", new String(payload, content.getRequestMessage().charset()));
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
        private final Map<String, String[]> modifiableParameters;
        private Map<String, String[]> allParameters = null;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public MessageSignVerifyRequestWrapper(HttpServletRequest request, Map<String, String[]> modifiableParameters, byte[] body) {
            super(request);
            this.modifiableParameters = Optional.ofNullable(modifiableParameters).orElse(new HashMap<>());
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

        @Override
        public String getParameter(String name) {
            String[] params = getParameterValues(name);
            if (params != null && params.length > 0) {
                return params[0];
            }
            return super.getParameter(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if (allParameters == null) {
                allParameters = new HashMap<>();
                allParameters.putAll(super.getParameterMap());
                allParameters.putAll(modifiableParameters);
            }
            return Collections.unmodifiableMap(allParameters);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(getParameterMap().keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            if (modifiableParameters.containsKey(name)) {
                return modifiableParameters.get(name);
            }
            return super.getParameterValues(name);
        }
    }

}
