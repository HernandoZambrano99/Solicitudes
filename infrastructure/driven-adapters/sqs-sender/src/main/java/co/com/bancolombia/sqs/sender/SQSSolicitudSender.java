package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.sqs.gateways.SolicitudSqsGateway;
import co.com.bancolombia.sqs.sender.config.SQSCapacidadProperties;
import co.com.bancolombia.sqs.sender.config.SQSSolicitudProperties;
import co.com.bancolombia.sqs.sender.dto.SolicitudesSqsDto;
import co.com.bancolombia.sqs.sender.mapper.SolicitudesSqsDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSolicitudSender implements SolicitudSqsGateway {

    private final SQSSolicitudProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;
    private final SolicitudesSqsDtoMapper solicitudesSqsDtoMapper;

    private Mono<String> send(String message) {
        return Mono.fromCallable(() ->
                        SendMessageRequest.builder()
                                .queueUrl(properties.queueUrl())
                                .messageBody(message)
                                .build())
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Mensaje enviado a SQS {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    @Override
    public Mono<Void> reportarSolicitudAprobada(SolicitudDetalle detalle) {
        return Mono.fromCallable(() -> {
                    SolicitudesSqsDto dto = solicitudesSqsDtoMapper.toDto(detalle);
                    log.info("Enviando solicitud {} a SQS para reportar solicitud aprobada",
                            dto.getSolicitudId());
                    return objectMapper.writeValueAsString(dto);
                })
                .flatMap(this::send)
                .then();
    }
}
