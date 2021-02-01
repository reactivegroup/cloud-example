package com.reactive.aws.ses;

import com.kevinten.vrml.core.serialization.Serialization;
import com.reactive.aws.ses.contract.SendEmailRequest;
import com.reactive.aws.ses.contract.SendEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * The Http/1.1 email api.
 */
@Slf4j
@RestController
public class AwsEmailController {

    @Autowired
    private AwsEmailService emailService;

    @GetMapping("checkHealth")
    public String checkHealth() {
        return "ok";
    }

    @PostMapping("sendEmail")
    public Mono<SendEmailResponse> sendEmail(@RequestBody SendEmailRequest sendEmailRequest) {
        log.info("[Http11EmailGatewayApi.sendEmail] request[{}]", Serialization.toJsonSafe(sendEmailRequest));
        return emailService.sendEmail(sendEmailRequest);
    }
}
