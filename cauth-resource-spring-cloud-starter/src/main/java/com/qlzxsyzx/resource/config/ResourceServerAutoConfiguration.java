package com.qlzxsyzx.resource.config;

import com.qlzxsyzx.resource.CustomJwtTokenEnhancer;
import com.qlzxsyzx.resource.properties.ClientDetailsProperties;
import com.qlzxsyzx.resource.properties.PermitAllProperties;
import com.qlzxsyzx.resource.UnauthorizedEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableConfigurationProperties({ClientDetailsProperties.class, PermitAllProperties.class})
@ConditionalOnWebApplication
@EnableResourceServer
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerAutoConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    private PermitAllProperties permitAllProperties;

    @Autowired
    private DefaultTokenServices defaultTokenServices;

    @Autowired
    private UnauthorizedEntryPoint unauthorizedEntryPoint;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenServices(defaultTokenServices);
        resources.resourceId("oauth2");
        resources.stateless(true);
        resources.authenticationEntryPoint(unauthorizedEntryPoint);
        super.configure(resources);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        for (String path : permitAllProperties.getPaths()) {
            http.authorizeRequests().antMatchers(path).permitAll();
        }
        http.csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        super.configure(http);
    }
}
