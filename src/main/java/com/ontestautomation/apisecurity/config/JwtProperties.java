package com.ontestautomation.apisecurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("bank.jwt")
public class JwtProperties {
    private String secret;
    private String issuer;
    private long expirySeconds;
}
