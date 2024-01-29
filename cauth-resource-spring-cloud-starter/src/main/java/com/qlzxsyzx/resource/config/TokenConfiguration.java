package com.qlzxsyzx.resource.config;

import com.qlzxsyzx.resource.CustomJwtTokenEnhancer;
import com.qlzxsyzx.resource.UnauthorizedEntryPoint;
import com.qlzxsyzx.resource.properties.ClientDetailsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@ConditionalOnWebApplication
public class TokenConfiguration {

    @Autowired
    private ClientDetailsProperties clientDetailsProperties;

    @Bean
    public CustomJwtTokenEnhancer customJwtTokenEnhancer() {
        CustomJwtTokenEnhancer customJwtTokenEnhancer = new CustomJwtTokenEnhancer();
        customJwtTokenEnhancer.setSigningKey("cauth");
        return customJwtTokenEnhancer;
    }

    @Bean
    public DefaultTokenServices defaultTokenServices(CustomJwtTokenEnhancer customJwtTokenEnhancer) {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(new JwtTokenStore(customJwtTokenEnhancer));
        return defaultTokenServices;
    }

    @Bean
    public UnauthorizedEntryPoint unauthorizedEntryPoint() {
        return new UnauthorizedEntryPoint(clientDetailsProperties);
    }
}
