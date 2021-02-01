package com.reactive.aws.xray.sdk2.config;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;

import javax.servlet.annotation.WebFilter;

@WebFilter
public class XRayFilter extends AWSXRayServletFilter {
}

