package co.com.bancolombia.sqs.sender;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.sqs.gateways.CapacidadSqsGateway;
import co.com.bancolombia.sqs.sender.config.SQSCapacidadProperties;
import co.com.bancolombia.sqs.sender.dto.EndeudamientoSqsDto;
import co.com.bancolombia.sqs.sender.mapper.EndeudamientoSqsDtoMapper;
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
public class SQSCapacidadSender implements CapacidadSqsGateway {

    private final SQSCapacidadProperties properties; // propiedades con la URL de tu cola CalculoEndeudamientoQueue
    private final SqsAsyncClient client;
    private final ObjectMapper objectMapper;
    private final EndeudamientoSqsDtoMapper mapper; // lo implementas igual que SolicitudesSqsDtoMapper

    private Mono<String> send(String message) {
        return Mono.fromCallable(() ->
                        SendMessageRequest.builder()
                                .queueUrl(properties.queueUrl()) // la URL de la cola de entrada
                                .messageBody(message)
                                .build())
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Mensaje enviado a SQS {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    @Override
    public Mono<Void> consultarCapacidadEndeudamiento(SolicitudDetalle detalle) {
        return Mono.fromCallable(() -> {
                    // Mapea el detalle a tu DTO de endeudamiento
                    EndeudamientoSqsDto dto = mapper.toDto(detalle);
                    log.info("Dto a enviar: " + dto.getIngresosTotales());

                    log.info("Enviando solicitud {} a SQS para cálculo de capacidad de endeudamiento",
                            dto.getSolicitudId());
                    String json = objectMapper.writeValueAsString(dto);
                    log.info("JSON enviado a SQS: {}", json);
                    return objectMapper.writeValueAsString(dto);
                })
                .flatMap(this::send)
                .then();
    }
}