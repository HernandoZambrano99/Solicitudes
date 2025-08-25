package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.bancolombia.r2dbc.entity.TipoPrestamoEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TipoPrestamoRepositoryAdapter
        extends ReactiveAdapterOperations<TipoPrestamo, TipoPrestamoEntity, Integer, TipoPrestamoDataRepository>
        implements TipoPrestamoRepository {

    public TipoPrestamoRepositoryAdapter(TipoPrestamoDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, TipoPrestamo.class));
    }
}