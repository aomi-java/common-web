package tech.aomi.common.web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用签名处理
 *
 * @author 田尘殇Sean(sean.snow @ live.com) createAt 2018/6/12
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableSignature {
}
