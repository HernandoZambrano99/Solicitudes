package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.EstadosEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EstadosDataRepository extends ReactiveCrudRepository<EstadosEntity, Integer>, ReactiveQueryByExampleExecutor<EstadosEntity> {
}