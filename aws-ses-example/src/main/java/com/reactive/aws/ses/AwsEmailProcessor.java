package com.reactive.aws.ses;

import com.kevinten.vrml.request.Requests;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.SesAsyncClientBuilder;
import software.amazon.awssdk.services.ses.model.*;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.reactive.aws.ses.ReactorFutureAdaptor.wrapCF;


/**
 * The Aws email processor.
 */
@Component
public class AwsEmailProcessor {

    private Map<Region, SesAsyncClient> sesAsyncClientMap;

    @PostConstruct
    public void init() {
        sesAsyncClientMap = new ConcurrentHashMap<>(4);
    }

    /**
     * The Aws email value.
     */
    @Data
    @Builder
    public static final class AwsEmailValue {

        private final Region region = Region.AP_SOUTHEAST_1;
        private final String charset = "UTF-8";

        private String configurationSetName;
    }

    public Mono<SendEmailResponse> send(String title, String content, String from, String to, AwsEmailValue valueSet) {
        SesAsyncClient sesAsyncClient = this.computeAsyncClient(valueSet);

        SendEmailRequest sendEmailRequest = this.makeAwsEmailRequest(title, content, from, to, valueSet);

        // sending with async non-blocking io
        Mono<SendEmailResponse> sendEmailResponseMono = Requests
                .of(() -> wrapCF(() -> sesAsyncClient.sendEmail(sendEmailRequest)))
                .get();
        return sendEmailResponseMono;
    }

    private SesAsyncClient computeAsyncClient(final AwsEmailValue valueSet) {
        return sesAsyncClientMap.computeIfAbsent(
                valueSet.getRegion(),
                this::createAsyncClient);
    }

    private SesAsyncClient createAsyncClient(Region region) {
        SesAsyncClientBuilder asyncClientBuilder = SesAsyncClient.builder()
                .region(region);

        this.createCustomProxy()
                .ifPresent(asyncClientBuilder::httpClientBuilder);

        return asyncClientBuilder.build();
    }

    private Optional<SdkAsyncHttpClient.Builder> createCustomProxy() {
        // FIXME: set proxy
        if (false) {
            return Optional.ofNullable(NettyNioAsyncHttpClient.builder()
                    .proxyConfiguration(ProxyConfiguration.builder()
                            .host("host")
                            .port(8080)
                            .build()));
        }
        return Optional.empty();
    }

    private SendEmailRequest makeAwsEmailRequest(String title, String content, String from, String
            to, AwsEmailValue valueSet) {
        return SendEmailRequest.builder()
                .destination(Destination.builder()
                        .toAddresses(to)
                        .build())
                .message(Message.builder()
                        .body(Body.builder()
                                .html(Content.builder()
                                        .charset(valueSet.getCharset())
                                        .data(content)
                                        .build())
                                .build())
                        .subject(Content.builder()
                                .charset(valueSet.getCharset())
                                .data(title)
                                .build())
                        .build())
                .source(from)
                .configurationSetName(valueSet.getConfigurationSetName())
                .build();
    }
}
