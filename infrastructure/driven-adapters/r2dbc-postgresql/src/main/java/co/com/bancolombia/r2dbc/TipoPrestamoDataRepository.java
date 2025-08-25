package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.TipoPrestamoEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TipoPrestamoDataRepository extends ReactiveCrudRepository<TipoPrestamoEntity, Integer>, ReactiveQueryByExampleExecutor<TipoPrestamoEntity> {
}