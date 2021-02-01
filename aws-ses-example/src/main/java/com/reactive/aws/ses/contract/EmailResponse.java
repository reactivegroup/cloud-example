package com.reactive.aws.ses.contract;

import lombok.Builder;
import lombok.Data;

/**
 * The Email Response Info.
 */
@Data
@Builder
public class EmailResponse {

    /**
     * Distributed unique tracking ID.
     */
    private String tracingId;

    /**
     * Mail sending platform tracking ID.
     */
    private String responseId;
}
