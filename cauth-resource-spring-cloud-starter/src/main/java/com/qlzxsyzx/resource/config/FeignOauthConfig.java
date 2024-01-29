package com.qlzxsyzx.resource.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@Configuration
public class FeignOauthConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 从请求头中获取access_token，并将其添加到请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            // 添加请求头，例如添加access_token
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object newCimAccessToken = request.getAttribute("new_cim_access_token");
            if (newCimAccessToken != null) {
                // refreshToken 后的转发请求，从attribute中取得access_token
                requestTemplate.header("Authorization", "Bearer " + newCimAccessToken);
                // 清除attribute中的access_token
                request.removeAttribute("new_cim_access_token");
            } else {
                String authorization = request.getHeader("Authorization");
                if (authorization != null) {
                    requestTemplate.header("Authorization", authorization);
                }
            }
        }
    }
}
