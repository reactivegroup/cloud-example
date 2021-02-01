package com.reactive.aws.xray.sdk2.service;

import com.reactive.aws.xray.sdk2.dao.XRayDao;
import com.reactive.aws.xray.sdk2.entity.XRayEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class XRayService {

    @Autowired
    private XRayDao xRayDao;

    public XRayEntity service() {
        return xRayDao.dao();
    }
}