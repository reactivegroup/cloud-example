package com.reactive.aws.ses.contract;

import lombok.Data;

/**
 * The Send Email Response.
 */
@Data
public class SendEmailResponse {

    /**
     * The response code.
     */
    private String code;

    /**
     * The response message.
     */
    private String message;

    /**
     * The response info.
     */
    private EmailResponse emailResponse;
}
