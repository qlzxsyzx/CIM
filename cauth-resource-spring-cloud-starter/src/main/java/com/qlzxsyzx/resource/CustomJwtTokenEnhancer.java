package com.qlzxsyzx.resource;

import com.qlzxsyzx.resource.entity.UserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.HashMap;
import java.util.Map;

public class CustomJwtTokenEnhancer extends JwtAccessTokenConverter {

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        OAuth2Authentication oAuth2Authentication = super.extractAuthentication(map);
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId((Long) map.get("userId"));
        ((AbstractAuthenticationToken) oAuth2Authentication.getUserAuthentication()).setDetails(userDetails);
        return oAuth2Authentication;
    }
}
