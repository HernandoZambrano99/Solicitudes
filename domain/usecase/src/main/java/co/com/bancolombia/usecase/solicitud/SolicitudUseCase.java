package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.estados.gateways.EstadosRepository;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.usecase.ValidarUsuarioUseCase;
import co.com.bancolombia.usecase.constants.LogsConstants;
import co.com.bancolombia.usecase.exceptions.MontoFueraDeRangoException;
import co.com.bancolombia.usecase.exceptions.SolicitudNotFoundException;
import co.com.bancolombia.usecase.exceptions.TipoPrestamoNotFoundException;
import co.com.bancolombia.usecase.exceptions.UsuarioNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private static final Logger logger = Logger.getLogger(SolicitudUseCase.class.getName());

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final EstadosRepository estadosRepository;
    private final ValidarUsuarioUseCase validarUsuarioUseCase;

    public Mono<SolicitudDetalle> ejecutar(Solicitud solicitud) {
        return validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad())
                .switchIfEmpty(Mono.error(new UsuarioNotFoundException(solicitud.getDocumentoIdentidad())))
                .zipWith(Mono.just(solicitud))
                .flatMap(tuple -> {
                    User usuario = tuple.getT1();
                    Solicitud sol = tuple.getT2();

                    return tipoPrestamoRepository.findById(sol.getIdTipoPrestamo())
                            .switchIfEmpty(Mono.error(new TipoPrestamoNotFoundException(sol.getIdTipoPrestamo())))
                            .flatMap(tipo -> validarMonto(sol, tipo)
                                    .map(solicitudValida -> new Object[]{ solicitudValida, usuario, tipo })
                            );
                })
                .flatMap(array -> {
                    Solicitud solicitudValida = (Solicitud) array[0];
                    User usuario = (User) array[1];
                    TipoPrestamo tipo = (TipoPrestamo) array[2];

                    solicitudValida.setIdEstado(1);

                    return solicitudRepository.save(solicitudValida)
                            .map(saved -> new Object[]{ saved, usuario, tipo });
                })
                .flatMap(array -> {
                    Solicitud saved = (Solicitud) array[0];
                    User usuario = (User) array[1];
                    TipoPrestamo tipo = (TipoPrestamo) array[2];

                    return Mono.zip(
                            estadosRepository.findById(saved.getIdEstado()),
                            Mono.just(tipo)
                    ).map(tuple -> {
                        logger.info(LogsConstants.REQUEST_SAVED_SUCCESS + saved.getIdSolicitud());
                        return SolicitudDetalle.builder()
                                .solicitud(saved)
                                .estado(tuple.getT1())
                                .tipoPrestamo(tuple.getT2())
                                .user(usuario)
                                .build();
                    });
                });
    }

    private Mono<Solicitud> validarMonto(Solicitud solicitud, TipoPrestamo tipo) {
        BigDecimal monto = solicitud.getMonto();
        BigDecimal minimo = tipo.getMontoMinimo();
        BigDecimal maximo = tipo.getMontoMaximo();

        if (monto.compareTo(minimo) < 0 || monto.compareTo(maximo) > 0) {
            logger.warning(LogsConstants.AMOUNT_OUT_RANGE + monto);
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