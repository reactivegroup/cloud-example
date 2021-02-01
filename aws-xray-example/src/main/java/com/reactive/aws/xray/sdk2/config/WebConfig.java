package com.reactive.aws.xray.sdk2.config;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * The Web config.
 */
@Configuration
public class WebConfig {

    /**
     * Tracing filter filter.
     */
    @Bean
    public Filter tracingFilter() {
        return new AWSXRayServletFilter("Scorekeep");
    }
}
