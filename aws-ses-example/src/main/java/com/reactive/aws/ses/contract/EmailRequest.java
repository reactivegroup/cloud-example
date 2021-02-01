package com.reactive.aws.ses.contract;

import lombok.Data;

/**
 * The Email Request Info.
 */
@Data
public class EmailRequest {

    /**
     * Distributed unique tracking ID.
     */
    private String tracingId;
}
