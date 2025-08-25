package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.SolicitudRequestDto;
import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final SolicitudUseCase solicitudUseCase;

    public Mono<ServerResponse> crearSolicitud(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SolicitudRequestDto.class)
                .map(dto -> Solicitud.builder()
                        .monto(dto.getMonto())
                        .plazo(dto.getPlazo())
                        .email(dto.getEmail())
                        .documentoIdentidad(dto.getDocumentoIdentidad())
                        .idTipoPrestamo(dto.getTipoPrestamoId())
                        .idEstado(1)
                        .build())
                .flatMap(solicitudUseCase::ejecutar)
                .flatMap(saved -> ServerResponse.ok().bodyValue(saved))
                .onErrorResume(error ->
                        ServerResponse.badRequest().bodyValue(error.getMessage()));
    }


    public Mono<ServerResponse> getSolicitudById(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return solicitudUseCase.buscarPorId(Integer.valueOf(id))
                .flatMap(solicitud -> ServerResponse.ok().bodyValue(solicitud))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(error ->
                        ServerResponse.status(500).bodyValue(error.getMessage()));
    }
}
