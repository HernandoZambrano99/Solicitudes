package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.r2dbc.entity.SolicitudEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SolicitudRepositoryAdapter
        extends ReactiveAdapterOperations<Solicitud, SolicitudEntity, Integer, SolicitudDataRepository>
        implements SolicitudRepository {

    public SolicitudRepositoryAdapter(SolicitudDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Solicitud.class));
    }
}