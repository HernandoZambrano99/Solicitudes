package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.SolicitudRequestDto;
import co.com.bancolombia.api.mapper.SolicitudMapper;
import co.com.bancolombia.api.mapper.SolicitudRequestMapper;
import co.com.bancolombia.model.estados.gateways.EstadosRepository;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoRepository;
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
    private final SolicitudMapper solicitudMapper;
    private final SolicitudRequestMapper solicitudRequestMapper;
    private final EstadosRepository estadosRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    public Mono<ServerResponse> crearSolicitud(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SolicitudRequestDto.class)
                .map(solicitudRequestMapper::toModel)
                .flatMap(solicitudUseCase::ejecutar)
                .flatMap(saved ->
                        Mono.zip(
                                estadosRepository.findById(saved.getIdEstado()),
                                tipoPrestamoRepository.findById(saved.getIdTipoPrestamo())
                        ).map(tuple -> solicitudMapper.toDto(saved, tuple.getT1(), tuple.getT2()))
                )
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
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
