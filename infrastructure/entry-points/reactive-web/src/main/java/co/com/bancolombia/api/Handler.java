package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.SolicitudRequestDto;
import co.com.bancolombia.api.exceptionHandler.RequestValidationException;
import co.com.bancolombia.api.mapper.SolicitudMapper;
import co.com.bancolombia.api.mapper.SolicitudRequestMapper;
import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.paginacion.SolicitudFilters;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final SolicitudUseCase solicitudUseCase;
    private final SolicitudMapper solicitudMapper;
    private final SolicitudRequestMapper solicitudRequestMapper;
    private final Validator validator;

    public Mono<ServerResponse> crearSolicitud(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers().firstHeader("Authorization");
        String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;

        return serverRequest.bodyToMono(SolicitudRequestDto.class)
                .flatMap(dto -> {
                    var violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        var details = violations.stream()
                                .map(v -> new RequestValidationException.FieldErrorDetail(
                                        v.getPropertyPath().toString(),
                                        v.getMessage()))
                                .toList();
                        return Mono.error(new RequestValidationException(details));
                    }
                    return Mono.just(dto);
                })
                .map(solicitudRequestMapper::toModel)
                .flatMap(solicitud -> solicitudUseCase.ejecutar(solicitud, jwt ))
                .map(solicitudDetalle -> solicitudMapper.toDto(
                        solicitudDetalle.getSolicitud(),
                        solicitudDetalle.getEstado(),
                        solicitudDetalle.getTipoPrestamo(),
                        solicitudDetalle.getUser()
                ))
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> getSolicitudById(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return solicitudUseCase.buscarPorId(Integer.valueOf(id))
                .flatMap(solicitud -> ServerResponse.ok().bodyValue(solicitud))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(error ->
                        ServerResponse.status(500).bodyValue(error.getMessage()));
    }

    public Mono<ServerResponse> getAllSolicitudesWithFilters(ServerRequest request) {
        // Extraer parámetros de paginación
        PageRequest pageRequest = PageRequest.builder()
                .pageSize(request.queryParam("pageSize")
                        .map(Integer::parseInt)
                        .orElse(10))
                .pageNumber(request.queryParam("pageNumber")
                        .map(Integer::parseInt)
                        .orElse(1))
                .build();
        Integer idEstado = Integer.valueOf(request.queryParam("status").orElse("1"));

        return solicitudUseCase.getSolicitudesByEstado(idEstado, pageRequest)
                .flatMap(pagedResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pagedResponse))
                .onErrorResume(error -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue("Error al obtener las solicitudes: " + error.getMessage()));
    }

}
