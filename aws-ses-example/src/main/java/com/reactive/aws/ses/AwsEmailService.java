package com.reactive.aws.ses;

import com.kevinten.vrml.core.serialization.Serialization;
import com.kevinten.vrml.core.tags.Fixme;
import com.kevinten.vrml.error.code.DefaultErrorCodes;
import com.reactive.aws.ses.contract.EmailResponse;
import com.reactive.aws.ses.contract.SendEmailRequest;
import com.reactive.aws.ses.contract.SendEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * The Aws email service.
 */
@Slf4j
@Component
public class AwsEmailService   {

    @Fixme(fixme = "set your own config")
    private static final String FROM_EMAIL_ADDRESS = "from@aws.com";
    private static final String AWS_CONFIGURATION_SET_NAME = "AwsConfigurationSet";

    @Autowired
    private AwsEmailProcessor awsEmailProcessor;

    public Mono<SendEmailResponse> sendEmail(SendEmailRequest sendEmailRequest) {
        this.checkParameters(sendEmailRequest);

        Mono<software.amazon.awssdk.services.ses.model.SendEmailResponse> send = this.send(sendEmailRequest);
        Mono<SendEmailResponse> doSend = send.map(sendEmailResponse -> this.onSuccess(sendEmailRequest, sendEmailResponse))
                .onErrorResume(throwable -> this.onFailure(sendEmailRequest, throwable))
                .log();
        return doSend;
    }

    private void checkParameters(SendEmailRequest sendEmailRequest) {
    }

    private Mono<software.amazon.awssdk.services.ses.model.SendEmailResponse> send(SendEmailRequest sendEmailRequest) {
        final String to = sendEmailRequest.getEmailDetail().getEmail();
        final String title = sendEmailRequest.getEmailDetail().getTitle();
        final String content = sendEmailRequest.getEmailDetail().getContent();

        Mono<software.amazon.awssdk.services.ses.model.SendEmailResponse> sendEmailResponseMono = awsEmailProcessor.send(title,
                content,
                FROM_EMAIL_ADDRESS,
                to,
                AwsEmailProcessor.AwsEmailValue.builder()
                        .configurationSetName(AWS_CONFIGURATION_SET_NAME)
                        .build())
                .doOnNext(sendEmailResponse -> log.info("[AwsEmailService.send] response[{}]", Serialization.toJsonSafe(sendEmailResponse)));
        return sendEmailResponseMono;
    }

    private SendEmailResponse onSuccess(SendEmailRequest sendEmailRequest, software.amazon.awssdk.services.ses.model.SendEmailResponse sendEmailResponse) {
        SendEmailResponse emailResponse = new SendEmailResponse();
        emailResponse.setCode(DefaultErrorCodes.SUCCESS.getCode());
        emailResponse.setMessage(DefaultErrorCodes.SUCCESS.getMessage());
        emailResponse.setEmailResponse(EmailResponse.builder()
                .tracingId(sendEmailRequest.getEmailRequest().getTracingId())
                .responseId(sendEmailResponse.messageId())
                .build());
        return emailResponse;
    }

    private Mono<? extends SendEmailResponse> onFailure(SendEmailRequest sendEmailRequest, Throwable throwable) {
        SendEmailResponse emailResponse = new SendEmailResponse();
        emailResponse.setCode(DefaultErrorCodes.SYSTEM_ERROR.getCode());
        emailResponse.setMessage(throwable.getMessage());
        emailResponse.setEmailResponse(EmailResponse.builder()
                .tracingId(sendEmailRequest.getEmailRequest().getTracingId())
                .build());
        return Mono.just(emailResponse);
    }
}
