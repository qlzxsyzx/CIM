package com.qlzxsyzx.resource.resolver;

import com.qlzxsyzx.resource.annotation.AuthenticationDetails;
import com.qlzxsyzx.resource.entity.UserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.HashMap;

public class AuthenticationDetailsResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(AuthenticationDetails.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        AuthenticationDetails annotation = methodParameter.getParameter().getAnnotation(AuthenticationDetails.class);
        String argName = annotation.value().trim();
        if (StringUtils.isEmpty(argName)) {
            argName = methodParameter.getParameter().getName();
        }
        // 从请求中获取认证信息，并将其封装为一个对象返回
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            Authentication userAuthentication = ((OAuth2Authentication) authentication).getUserAuthentication();
            Object details = userAuthentication.getDetails();
            if (details instanceof UserDetails) {
                Class<?> aClass = details.getClass();
                try {
                    Field valueField = aClass.getDeclaredField(argName);
                    valueField.setAccessible(true);
                    return valueField.get(details);
                } catch (NoSuchFieldException ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
