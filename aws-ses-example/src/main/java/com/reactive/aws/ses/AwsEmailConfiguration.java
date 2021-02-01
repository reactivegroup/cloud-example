package com.reactive.aws.ses;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * The Default aws email configuration.
 */
@Configuration
public class AwsEmailConfiguration {

    private static final String ACCESS_KEY_ID = "123";
    private static final String SECRET_KEY = "123/456";

    @PostConstruct
    public void init() {
        System.setProperty("aws.accessKeyId", ACCESS_KEY_ID);
        System.setProperty("aws.secretKey", SECRET_KEY);
    }
}
