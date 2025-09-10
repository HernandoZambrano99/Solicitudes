package co.com.bancolombia.api;

import co.com.bancolombia.api.constants.AppConstants;
import co.com.bancolombia.api.constants.ErrorConstants;
import co.com.bancolombia.api.dto.EstadoSolicitudRequestDto;
import co.com.bancolombia.api.dto.SolicitudRequestDto;
import co.com.bancolombia.api.dto.SolicitudResponseDto;
import co.com.bancolombia.api.exceptionHandler.InvalidJwtTokenException;
import co.com.bancolombia.api.exceptionHandler.InvalidParameterException;
import co.com.bancolombia.api.exceptionHandler.RequestValidationException;
import co.com.bancolombia.api.mapper.SolicitudMapper;
import co.com.bancolombia.api.mapper.SolicitudRequestMapper;
import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.paginacion.PagedResponse;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Handler {

    private final SolicitudUseCase solicitudUseCase;
    private final SolicitudMapper solicitudMapper;
    private final SolicitudRequestMapper solicitudRequestMapper;
    private final Validator validator;

    public Mono<ServerResponse> crearSolicitud(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers().firstHeader(AppConstants.AUTHORIZATION_HEADER);
        String jwt = (authHeader != null && authHeader.startsWith(AppConstants.BEARER)) ? authHeader.substring(7) : null;

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
        String authHeader = request.headers().firstHeader(AppConstants.AUTHORIZATION_HEADER);
        String jwt = (authHeader != null && authHeader.startsWith(AppConstants.BEARER)) ? authHeader.substring(7) : null;

        PageRequest pageRequest = PageRequest.builder()
                .pageSize(request.queryParam(AppConstants.PAGE_SIZE)
                        .map(Integer::parseInt)
                        .orElse(10))
                .pageNumber(request.queryParam(AppConstants.PAGE_NUMBER)
                        .map(Integer::parseInt)
                        .orElse(1))
                .build();

        List<String> rawStatuses = request.queryParams().get(AppConstants.STATUS_FILTER);

        List<Integer> estados;
        if (rawStatuses == null || rawStatuses.isEmpty()) {
            estados = List.of(1,3,4);
        } else {
            estados = rawStatuses.stream()
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .filter(str -> !str.isBlank())
                    .map(Integer::parseInt)
                    .toList();
        }

        return solicitudUseCase.getSolicitudesByEstado(estados, pageRequest, jwt)
                .map(pagedResponse -> {
                    List<SolicitudResponseDto> dtoList = pagedResponse.getData().stream()
                            .map(detalle -> solicitudMapper.toDto(
                                    detalle.getSolicitud(),
                                    detalle.getEstado(),
                                    detalle.getTipoPrestamo(),
                                    detalle.getUser()))
                            .toList();

                    return PagedResponse.<SolicitudResponseDto>builder()
                            .pageNumber(pagedResponse.getPageNumber())
                            .pageSize(pagedResponse.getPageSize())
                            .totalRecords(pagedResponse.getTotalRecords())
                            .totalPages(pagedResponse.getTotalPages())
                            .data(dtoList)
                            .build();
                })
                .flatMap(pagedDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pagedDto));
    }

    public Mono<ServerResponse> aprobarORechazarSolicitud(ServerRequest serverRequest) {
        String authHeader = serverRequest.headers().firstHeader(AppConstants.AUTHORIZATION_HEADER);
        String jwt = (authHeader != null && authHeader.startsWith(AppConstants.BEARER))
                ? authHeader.substring(7)
                : null;

        if (jwt == null) {
            return Mono.error(new InvalidJwtTokenException(ErrorConstants.AUTHORIZATION_NOT_FOUND));
        }

        String idSolicitudStr = serverRequest.pathVariable(AppConstants.REQUEST_ID);
        if (idSolicitudStr == null || idSolicitudStr.isBlank()) {
            return Mono.error(new InvalidParameterException(ErrorConstants.ID_IS_MANDATORY));
        }

        Integer idSolicitud;
        try {
            idSolicitud = Integer.valueOf(idSolicitudStr);
        } catch (NumberFormatException e) {
            return Mono.error(new InvalidParameterException(ErrorConstants.NO_NUMERIC_ID));
        }

        return serverRequest.bodyToMono(EstadoSolicitudRequestDto.class)
                .switchIfEmpty(Mono.error(new InvalidParameterException(ErrorConstants.BODY_IS_MANDATORY)))
                .flatMap(req -> solicitudUseCase.aprobarORechazar(idSolicitud, req.getNuevoEstado(), jwt))
                .map(detalle -> solicitudMapper.toDto(
                        detalle.getSolicitud(),
                        detalle.getEstado(),
                        detalle.getTipoPrestamo(),
                        detalle.getUser()
                ))
                .flatMap(dto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dto));
    }

}
