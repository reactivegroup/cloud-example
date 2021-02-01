package com.reactive.aws.xray.sdk2.controller;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.reactive.aws.xray.sdk2.entity.XRayEntity;
import com.reactive.aws.xray.sdk2.service.XRayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@XRayEnabled
@RestController
public class XRayController {

    @Autowired
    private XRayService xRayService;

    @RequestMapping("xray")
    public XRayEntity controller() {
        log.info("x-ray sdk2 request " + System.currentTimeMillis());
        return xRayService.service();
    }
}
