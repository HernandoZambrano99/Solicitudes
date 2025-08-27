package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.estados.gateways.EstadosRepository;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.bancolombia.usecase.constants.LogsConstants;
import co.com.bancolombia.usecase.exceptions.MontoFueraDeRangoException;
import co.com.bancolombia.usecase.exceptions.SolicitudNotFoundException;
import co.com.bancolombia.usecase.exceptions.TipoPrestamoNotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private static final Logger logger = Logger.getLogger(SolicitudUseCase.class.getName());

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final EstadosRepository estadosRepository;

    public Mono<SolicitudDetalle> ejecutar(Solicitud solicitud) {
        return tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                .switchIfEmpty(Mono.error(new TipoPrestamoNotFoundException(solicitud.getIdTipoPrestamo())))
                .flatMap(tipo -> validarMonto(solicitud, tipo))
                .flatMap(solicitudValida -> {
                    solicitudValida.setIdEstado(1);
                    return solicitudRepository.save(solicitudValida);
                })
                .flatMap(saved ->
                        Mono.zip(
                                estadosRepository.findById(saved.getIdEstado()),
                                tipoPrestamoRepository.findById(saved.getIdTipoPrestamo())
                        ).map(tuple -> {
                            logger.info(LogsConstants.REQUEST_SAVED_SUCCESS + saved.getIdSolicitud());
                            return SolicitudDetalle.builder()
                                    .solicitud(saved)
                                    .estado(tuple.getT1())
                                    .tipoPrestamo(tuple.getT2())
                                    .build();
                        })
                );
    }

    private Mono<Solicitud> validarMonto(Solicitud solicitud, TipoPrestamo tipo) {
        Double monto = solicitud.getMonto();
        if (monto < tipo.getMontoMinimo() || monto > tipo.getMontoMaximo()) {
            logger.warning(LogsConstants.AMOUNT_OUT_RANGE + monto);
            return Mono.error(new MontoFueraDeRangoException(monto, tipo.getMontoMinimo(), tipo.getMontoMaximo()));
        }
        solicitud.setIdTipoPrestamo(tipo.getIdTipoPrestamo());
        return Mono.just(solicitud);
    }

    public Mono<Solicitud> buscarPorId(Integer id) {
        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new SolicitudNotFoundException(id)));
    }
}