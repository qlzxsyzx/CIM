package com.qlzxsyzx.resource.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.details")
public class ClientDetailsProperties {

    /**
     * 客户端ID
     */
    private String clientId = "c-im";

    /**
     * 客户端密钥
     */
    private String clientSecret = "c-im-secret";

    /**
     * check_token_uri
     */
    private String checkTokenUri = "http://localhost:9999/oauth/check_token";

    /**
     * refresh token uri
     */
    private String tokenUri = "http://localhost:9999/oauth/token";

    public ClientDetailsProperties() {
    }

    public ClientDetailsProperties(String clientId, String clientSecret, String checkTokenUri, String tokenUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.checkTokenUri = checkTokenUri;
        this.tokenUri = tokenUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getCheckTokenUri() {
        return checkTokenUri;
    }

    public void setCheckTokenUri(String checkTokenUri) {
        this.checkTokenUri = checkTokenUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }
}
