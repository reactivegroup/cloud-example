package com.reactive.aws.xray.sdk2.dao;

import com.reactive.aws.xray.sdk2.entity.XRayEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class XRayDao {

    public XRayEntity dao() {
        XRayEntity xRayEntity = new XRayEntity();
        xRayEntity.setUuid(UUID.randomUUID().toString());
        return xRayEntity;
    }
}
