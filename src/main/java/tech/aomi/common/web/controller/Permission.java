package tech.aomi.common.web.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;


/**
 * @author Sean Create At 2020/1/15
 */
@Getter
@Setter
@AllArgsConstructor
public class Permission {

    private HttpMethod method;

    private String[] antPatterns;

    private Enum<?>[] authorities;

    public static Permission get(String[] antPatterns, Enum<?>[] authorities) {
        return new Permission(HttpMethod.GET, antPatterns, authorities);
    }

    public static Permission post(String[] antPatterns, Enum<?>[] authorities) {
        return new Permission(HttpMethod.POST, antPatterns, authorities);
    }

    public static Permission put(String[] antPatterns, Enum<?>[] authorities) {
        return new Permission(HttpMethod.PUT, antPatterns, authorities);
    }

    public static Permission patch(String[] antPatterns, Enum<?>[] authorities) {
        return new Permission(HttpMethod.PATCH, antPatterns, authorities);
    }

    public static Permission delete(String[] antPatterns, Enum<?>[] authorities) {
        return new Permission(HttpMethod.DELETE, antPatterns, authorities);
    }

}
