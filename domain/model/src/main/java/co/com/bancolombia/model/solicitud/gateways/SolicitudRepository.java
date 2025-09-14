package co.com.bancolombia.model.solicitud.gateways;

import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.solicitud.Solicitud;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

public interface SolicitudRepository {
    Mono<Solicitud> save(Solicitud solicitud);
    Flux<Solicitud> findAll();
    Mono<Solicitud> findById(Integer id);

    Flux<Solicitud> findByIdEstadoPaged(List<Integer> idEstado, PageRequest pageRequest);
    Mono<Long> countByIdEstado(List<Integer> idEstado);
    Flux<Solicitud> findSolicitudesAprobadasByUsuario(String documentoIdentidad, Integer aprobado);
}