package co.com.bancolombia.model.solicitud.gateways;

import co.com.bancolombia.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface SolicitudRepository {
    Mono<Solicitud> save(Solicitud solicitud);
    Flux<Solicitud> findAll();
    Mono<Solicitud> findById(Integer id);
}