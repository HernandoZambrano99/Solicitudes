package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.sqs.gateways.SqsGateway;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
import co.com.bancolombia.sqs.sender.dto.SolicitudesSqsDto;
import co.com.bancolombia.sqs.sender.mapper.SolicitudesSqsDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements SqsGateway {

    private final SQSSenderProperties properties;
    private final software.amazon.awssdk.services.sqs.SqsAsyncClient client;
    private final ObjectMapper objectMapper;
    private final SolicitudesSqsDtoMapper solicitudesSqsDtoMapper;

    /**
     * Método genérico para enviar cualquier string
     */
    public Mono<String> send(String message) {
        return Mono.fromCallable(() ->
                        software.amazon.awssdk.services.sqs.model.SendMessageRequest.builder()
                                .queueUrl(properties.queueUrl())
                                .messageBody(message)
                                .build())
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Message sent to SQS {}", response.messageId()))
                .map(software.amazon.awssdk.services.sqs.model.SendMessageResponse::messageId);
    }

    /**
     * Implementación del puerto SqsGateway
     */
    @Override
    public Mono<Void> enviarSolicitudActualizada(SolicitudDetalle detalle) {
        return Mono.fromCallable(() -> {
                     SolicitudesSqsDto dto = solicitudesSqsDtoMapper.toDto(detalle);
                    log.info("Enviando solicitud {} con email {}", dto.getSolicitudId(), detalle.getUser().getEmail());
                    return objectMapper.writeValueAsString(dto);
                })
                .flatMap(this::send)
                .then();
    }

}