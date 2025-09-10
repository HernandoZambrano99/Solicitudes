package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.sqs.gateways.SqsGateway;
import co.com.bancolombia.sqs.sender.config.SQSSenderProperties;
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
                    // Mapea lo que quieres mandar a la Lambda
                    var dto = new MensajeSolicitud(
                            detalle.getSolicitud().getIdSolicitud(),
                            detalle.getEstado().getNombre(),
                            detalle.getUser().getEmail()
                    );
                    log.info("Enviando solicitud {} con email {}", detalle.getSolicitud().getIdSolicitud(), detalle.getUser().getEmail());
                    return objectMapper.writeValueAsString(dto);
                })
                .flatMap(this::send)
                .then();
    }

    /**
     * DTO con la estructura esperada por la Lambda
     */
    record MensajeSolicitud(Integer solicitudId, String estado, String correoCliente) {
    }
}