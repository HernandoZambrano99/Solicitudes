package co.com.bancolombia.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.calculator")
public record SQSCapacidadProperties(
        String queueUrl
)
{}