package co.com.bancolombia.sqs.listener;

import co.com.bancolombia.model.resultado.ResultadoSolicitud;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final SolicitudUseCase solicitudUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> apply(Message message) {
        return Mono.fromCallable(() -> {
                    String body = message.body();
                    return objectMapper.readValue(body, ResultadoSolicitud.class);
                })
                .flatMap(solicitudUseCase::actualizarEstadoConResultado)
                .onErrorResume(e -> {
                    log.error("[SQS] Error procesando mensaje", e);
                    return Mono.empty();
                });
    }
}