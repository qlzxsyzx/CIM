package com.qlzxsyzx.resource;

import com.qlzxsyzx.resource.constant.TokenConstant;
import com.qlzxsyzx.resource.properties.ClientDetailsProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UnauthorizedEntryPoint extends OAuth2AuthenticationEntryPoint {
    private final ClientDetailsProperties clientDetailsProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private DefaultTokenServices defaultTokenServices;

    public UnauthorizedEntryPoint(ClientDetailsProperties clientDetailsProperties) {
        this.clientDetailsProperties = clientDetailsProperties;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof InsufficientAuthenticationException) {
            // access_token缺失或者过期
            refreshToken(request, response);
        } else {
            super.commence(request, response, authException);
        }
    }

    private void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            LinkedMultiValueMap<String, String> formData = getStringStringLinkedMultiValueMap(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<LinkedMultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            ResponseEntity<OAuth2AccessToken> entity1 = restTemplate.postForEntity(clientDetailsProperties.getTokenUri(), entity, OAuth2AccessToken.class);
            if (entity1.getStatusCode() != HttpStatus.OK) {
                response.setStatus(entity1.getStatusCodeValue());
                response.setHeader("Content-Type", "application/json");
                response.getWriter().write(new ObjectMapper().writeValueAsString(entity1.getBody()));
                response.getWriter().flush();
            } else {
                // 刷新token成功，存入cookie
                String accessToken = entity1.getBody().getValue();
                String refreshToken = entity1.getBody().getRefreshToken().getValue();
                setCookie(response, TokenConstant.ACCESS_TOKEN_NAME, accessToken, TokenConstant.ACCESS_TOKEN_EXPIRE);
                setCookie(response, TokenConstant.REFRESH_TOKEN_NAME, refreshToken, TokenConstant.REFRESH_TOKEN_EXPIRE);
                // 重新执行原来的request
                // 解析access_token为authentication，设置进去
                OAuth2Authentication oAuth2Authentication = defaultTokenServices.loadAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(oAuth2Authentication);
                request.setAttribute("new_cim_access_token", accessToken);
                request.getRequestDispatcher(request.getRequestURI()).forward(request, response);
            }
        } catch (Exception re) {
            response.setStatus(401);
            response.getWriter().flush();
            throw re;
        }
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private LinkedMultiValueMap<String, String> getStringStringLinkedMultiValueMap(HttpServletRequest request) {
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientDetailsProperties.getClientId());
        formData.add("client_secret", clientDetailsProperties.getClientSecret());
        formData.add("grant_type", "refresh_token");
        Cookie[] cookies = request.getCookies();
        Assert.notNull(cookies, "Cookie不能为空");
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TokenConstant.REFRESH_TOKEN_NAME)) {
                formData.add("refresh_token", cookie.getValue());
                break;
            }
        }
        return formData;
    }
}
