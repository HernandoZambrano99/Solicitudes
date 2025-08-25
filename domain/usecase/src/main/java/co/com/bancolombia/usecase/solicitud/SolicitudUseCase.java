package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SolicitudUseCase {

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;

    public Mono<Solicitud> ejecutar(Solicitud solicitud) {

        return tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())
                .switchIfEmpty(Mono.error(new RuntimeException("El tipo de préstamo no existe")))
                .flatMap(tipo -> validarMonto(solicitud, tipo))
                .flatMap(solicitudValida -> {
                    solicitudValida.setIdEstado(1);
                    return solicitudRepository.save(solicitudValida);
                });
    }

    private Mono<Solicitud> validarMonto(Solicitud solicitud, TipoPrestamo tipo) {
        Double monto = solicitud.getMonto();
        if (monto < tipo.getMontoMinimo() || monto > tipo.getMontoMaximo()) {
            return Mono.error(new RuntimeException("Monto fuera de rango permitido"));
        }
        solicitud.setIdTipoPrestamo(tipo.getIdTipoPrestamo());
        return Mono.just(solicitud);
    }

    public Mono<Solicitud> buscarPorId(Integer id) {
        return solicitudRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Solicitud no encontrada")));
    }
}