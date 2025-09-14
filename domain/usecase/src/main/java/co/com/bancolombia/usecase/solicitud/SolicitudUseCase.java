package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.estados.gateways.EstadosRepository;
import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.paginacion.PagedResponse;
import co.com.bancolombia.model.resultado.ResultadoSolicitud;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.sqs.gateways.CapacidadSqsGateway;
import co.com.bancolombia.model.sqs.gateways.SqsGateway;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.usecase.ValidarUsuarioUseCase;
import co.com.bancolombia.usecase.constants.Constants;
import co.com.bancolombia.usecase.enums.EstadoSolicitudEnum;
import co.com.bancolombia.usecase.exceptions.*;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private static final Logger logger = Logger.getLogger(SolicitudUseCase.class.getName());

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final EstadosRepository estadosRepository;
    private final ValidarUsuarioUseCase validarUsuarioUseCase;
    private final SqsGateway sqsGateway;
    private final CapacidadSqsGateway capacidadSqsGateway;

    public Mono<SolicitudDetalle> ejecutar(Solicitud solicitud) {
        return validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad())
                .switchIfEmpty(Mono.error(new UsuarioNotFoundException(solicitud.getDocumentoIdentidad())))
                .flatMap(usuario ->
                        validarUsuarioUseCase.validarSiCoincideConJwt(solicitud.getDocumentoIdentidad())
                                .flatMap(match -> {
                                    if (!match) {
                                        return Mono.error(new UsuarioNoCoincideException(solicitud.getDocumentoIdentidad()));
                                    }
                                    return Mono.just(usuario);
                                })
                                .zipWith(Mono.just(solicitud))
                )
                .flatMap(tuple -> {
                    User usuario = tuple.getT1();
                    Solicitud sol = tuple.getT2();

                    return tipoPrestamoRepository.findById(sol.getIdTipoPrestamo())
                            .switchIfEmpty(Mono.error(new TipoPrestamoNotFoundException(sol.getIdTipoPrestamo())))
                            .flatMap(tipo -> validarMonto(sol, tipo)
                                    .map(solicitudValida -> new Object[]{solicitudValida, usuario, tipo})
                            );
                })
                .flatMap(array -> {
                    Solicitud solicitudValida = (Solicitud) array[0];
                    User usuario = (User) array[1];
                    TipoPrestamo tipo = (TipoPrestamo) array[2];

                    solicitudValida.setIdEstado(1);

                    return solicitudRepository.save(solicitudValida)
                            .map(saved -> new Object[]{saved, usuario, tipo});
                })
                .flatMap(array -> {
                    Solicitud saved = (Solicitud) array[0];
                    User usuario = (User) array[1];
                    TipoPrestamo tipo = (TipoPrestamo) array[2];
                    validarYEnviarCapacidadEndeudamiento(saved.getIdSolicitud()); // Llama al metodo que revisa si tiene validacion automatica o no
                    return Mono.zip(
                            estadosRepository.findById(saved.getIdEstado()),
                            Mono.just(tipo)
                    ).map(tuple -> {
                        logger.info(Constants.REQUEST_SAVED_SUCCESS + saved.getIdSolicitud());
                        return SolicitudDetalle.builder()
                                .solicitud(saved)
                                .estado(tuple.getT1())
                                .tipoPrestamo(tuple.getT2())
                                .user(usuario)
                                .build();
                    });
                });
    }

    public Mono<PagedResponse<SolicitudDetalle>> getSolicitudesByEstado(
            List<Integer> idEstados, PageRequest pageRequest) {
        logger.info(() -> String.format(
                Constants.GET_SOLICITUDES_BY_ESTADO_INIT,
                idEstados, pageRequest.getPageNumber(), pageRequest.getPageSize()
        ));
        return solicitudRepository.countByIdEstado(idEstados)
                .flatMap(totalRecords -> {
                    if (totalRecords == 0) {
                        logger.info(() -> String.format(
                                Constants.GET_SOLICITUDES_BY_ESTADO_EMPTY, idEstados
                        ));
                        return Mono.just(PagedResponse.<SolicitudDetalle>builder()
                                .pageNumber(pageRequest.getPageNumber())
                                .pageSize(pageRequest.getPageSize())
                                .totalRecords(0L)
                                .totalPages(0)
                                .data(Collections.emptyList())
                                .build());
                    }

                    int totalPages = (int) Math.ceil((double) totalRecords / pageRequest.getPageSize());

                    return solicitudRepository.findByIdEstadoPaged(idEstados, pageRequest)
                            .flatMap(solicitud ->
                                    Mono.zip(
                                            estadosRepository.findById(solicitud.getIdEstado()),
                                            tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo()),
                                            validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad())
                                    ).map(tuple -> SolicitudDetalle.builder()
                                            .solicitud(solicitud)
                                            .estado(tuple.getT1())
                                            .tipoPrestamo(tuple.getT2())
                                            .user(tuple.getT3())
                                            .build())
                            )
                            .collectList()
                            .map(detalles -> PagedResponse.<SolicitudDetalle>builder()
                                    .pageNumber(pageRequest.getPageNumber())
                                    .pageSize(pageRequest.getPageSize())
                                    .totalRecords(totalRecords)
                                    .totalPages(totalPages)
                                    .data(detalles)
                                    .build());
                });
    }

    public Mono<SolicitudDetalle> aprobarORechazar(Integer idSolicitud, String nuevoEstado) {
        return buscarPorId(idSolicitud)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(idSolicitud)))
                .flatMap(solicitud -> {
                    EstadoSolicitudEnum estadoEnum = EstadoSolicitudEnum.fromString(nuevoEstado);
                    solicitud.setIdEstado(estadoEnum.getIdEstado());
                    logger.info(() -> String.format(Constants.APROBAR_RECHAZAR_ESTADO_ACTUALIZADO,
                            solicitud.getIdSolicitud(),
                            nuevoEstado));
                    return solicitudRepository.save(solicitud);
                })
                .flatMap(saved -> Mono.zip(
                        estadosRepository.findById(saved.getIdEstado()),
                        tipoPrestamoRepository.findById(saved.getIdTipoPrestamo()),
                        validarUsuarioUseCase.validarSiExiste(saved.getDocumentoIdentidad())
                ).flatMap(tuple -> {
                    SolicitudDetalle detalle = SolicitudDetalle.builder()
                            .solicitud(saved)
                            .estado(tuple.getT1())
                            .tipoPrestamo(tuple.getT2())
                            .user(tuple.getT3())
                            .build();
                    return enviarMensajeSQS(detalle).thenReturn(detalle);
                })).doOnError(e -> logger.severe(() -> String.format(Constants.APROBAR_RECHAZAR_ERROR,
                        idSolicitud,
                        e.getMessage())));
    }

    public Mono<Void> validarYEnviarCapacidadEndeudamiento(Integer idSolicitud) {
        logger.info("Entrando a validarYEnviarCapacidadEndeudamiento con id " + idSolicitud);
        return solicitudRepository.findById(idSolicitud)
                .flatMap(solicitud -> tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                        .flatMap(tipo -> {
                            logger.info("Tipo préstamo " + tipo.getNombre() +
                                    " validacionAutomatica=" + tipo.getValidacionAutomatica());
                            if (Boolean.TRUE.equals(tipo.getValidacionAutomatica())) {
                                logger.info("Llamando enviarCapacidadEndeudamiento...");
                                return enviarCapacidadEndeudamiento(idSolicitud)
                                        .doOnSuccess(v -> logger.info("Capacidad de endeudamiento enviada para solicitud " + idSolicitud))
                                        .doOnError(e -> logger.severe("Error enviando capacidad de endeudamiento: " + e.getMessage()));
                            }
                            // si no es automática, no hacemos nada
                            return Mono.empty();
                        }))
                .then(); // devolvemos Mono<Void>
    }

    public Mono<Void> enviarCapacidadEndeudamiento(Integer idSolicitud) {
        // 1. Buscar solicitud por id
        return solicitudRepository.findById(idSolicitud)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(idSolicitud)))
                .flatMap(solicitud -> {
                    // 2. Buscar usuario que hizo la solicitud
                    return validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad())
                            .switchIfEmpty(Mono.error(new UsuarioNotFoundException(solicitud.getDocumentoIdentidad())))
                            .flatMap(usuario -> {
                                // 3. Buscar préstamos aprobados del usuario
                                return solicitudRepository
                                        .findSolicitudesAprobadasByUsuario(
                                                solicitud.getDocumentoIdentidad(), EstadoSolicitudEnum.APROBADO.getIdEstado())
                                        .collectList()
                                        .flatMap(prestamosAprobados -> {
                                            // 4. Armar SolicitudDetalle con todo
                                            return Mono.zip(
                                                    estadosRepository.findById(solicitud.getIdEstado()),
                                                    tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                                            ).flatMap(tuple -> {
                                                SolicitudDetalle detalle = SolicitudDetalle.builder()
                                                        .solicitud(solicitud)
                                                        .estado(tuple.getT1())
                                                        .tipoPrestamo(tuple.getT2())
                                                        .user(usuario)
                                                        .prestamosAprobados(prestamosAprobados)
                                                        .build();
                                                // 5. Enviar a la cola de endeudamiento
                                                return capacidadSqsGateway.consultarCapacidadEndeudamiento(detalle);
                                            });
                                        });
                            });
                });
    }

    private Mono<Void> enviarMensajeSQS(SolicitudDetalle detalle) {
        return sqsGateway.enviarSolicitudActualizada(detalle);
    }

    public Mono<Void> actualizarEstadoConResultado(ResultadoSolicitud result) {
        return solicitudRepository.findById(result.getSolicitudId())
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(result.getSolicitudId())))
                .flatMap(solicitud -> {
                    // conviertes la decision a tu enum
                    EstadoSolicitudEnum estadoEnum = EstadoSolicitudEnum.fromString(result.getDecision());
                    solicitud.setIdEstado(estadoEnum.getIdEstado());
                    return solicitudRepository.save(solicitud);
                })
                .doOnSuccess(saved -> {
                    // java.util.logging.Logger: concatenar Strings
                    logger.info("Solicitud " + result.getSolicitudId()
                            + " actualizada con éxito a estado " + result.getDecision());
                })
                .doOnError(e -> {
                    logger.severe("Error actualizando solicitud " + result.getSolicitudId() + ": " + e.getMessage());
                })
                .then(); // devolvemos Mono<Void>
    }
    private Mono<Solicitud> validarMonto(Solicitud solicitud, TipoPrestamo tipo) {
        BigDecimal monto = solicitud.getMonto();
        BigDecimal minimo = tipo.getMontoMinimo();
        BigDecimal maximo = tipo.getMontoMaximo();

        if (monto.compareTo(minimo) < 0 || monto.compareTo(maximo) > 0) {
            logger.warning(Constants.AMOUNT_OUT_RANGE + monto);
            return Mono.error(new MontoFueraDeRangoException(monto, minimo, maximo));
        }

        solicitud.setIdTipoPrestamo(tipo.getIdTipoPrestamo());
        return Mono.just(solicitud);
    }

    public Mono<Solicitud> buscarPorId(Integer id) {
        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)));
    }
}