package co.com.bancolombia.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.solicitud")
public record SQSSolicitudProperties(
        String queueUrl
)
{}