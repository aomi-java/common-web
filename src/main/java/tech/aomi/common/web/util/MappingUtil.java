package tech.aomi.common.web.util;

import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 从RequestMapping中获取所有的请求信息
 *
 * @author createAt 2018/6/19
 */
public class MappingUtil {

    /**
     * 获取所有映射信息
     * uri: 地址
     * method: 请求方法
     *
     * @param requestMappingHandlerMapping RequestMappingHandlerMapping
     * @return 获取所有映射信息
     */
    public static Set<Map<String, String>> allMappings(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        Set<Map<String, String>> mappings = new HashSet<>();
        requestMappingHandlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
            RequestMethodsRequestCondition methodsRequestCondition = requestMappingInfo.getMethodsCondition();
            requestMappingInfo.getPatternValues().forEach(uri -> {
                methodsRequestCondition.getMethods().forEach(method -> {
                    Map<String, String> mapping = new HashMap<>();
                    mapping.put("uri", uri);
                    mapping.put("method", method.toString());
                    mappings.add(mapping);
                });

            });

        });

        return mappings;
    }

}
