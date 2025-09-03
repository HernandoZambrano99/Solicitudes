package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.r2dbc.entity.SolicitudEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SolicitudRepositoryAdapter
        extends ReactiveAdapterOperations<Solicitud, SolicitudEntity, Integer, SolicitudDataRepository>
        implements SolicitudRepository {

    public SolicitudRepositoryAdapter(SolicitudDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
    }

    @Override
    public Flux<Solicitud> findByIdEstadoPaged(Integer idEstado, PageRequest pageRequest) {
        int offset = (pageRequest.getPageNumber() - 1) * pageRequest.getPageSize();
        return repository.findSolicitudesParaRevision(idEstado)
                .skip(offset)
                .take(pageRequest.getPageSize())
                .map(entity -> mapper.map(entity, Solicitud.class));
    }

    @Override
    public Mono<Long> countByIdEstado(Integer idEstado) {
        return repository.findSolicitudesParaRevision(idEstado)
                .count();
    }
}