package com.reactive.aws.ses.contract;

import lombok.Data;

/**
 * The Send Email Request.
 */
@Data
public class SendEmailRequest {

    /**
     * Email request info.
     */
    private EmailRequest emailRequest;

    /**
     * Email detail info.
     */
    private EmailDetail emailDetail;
}
