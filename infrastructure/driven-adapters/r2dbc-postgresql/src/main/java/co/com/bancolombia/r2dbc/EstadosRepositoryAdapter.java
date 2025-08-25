package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.estados.gateways.EstadosRepository;
import co.com.bancolombia.r2dbc.entity.EstadosEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EstadosRepositoryAdapter
        extends ReactiveAdapterOperations<Estados, EstadosEntity, Integer, EstadosDataRepository>
        implements EstadosRepository {

    public EstadosRepositoryAdapter(EstadosDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Estados.class));
    }
}
