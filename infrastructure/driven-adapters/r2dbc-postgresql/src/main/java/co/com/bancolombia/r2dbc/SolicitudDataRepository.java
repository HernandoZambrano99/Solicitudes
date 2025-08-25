package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.SolicitudEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SolicitudDataRepository extends ReactiveCrudRepository<SolicitudEntity, Integer>, ReactiveQueryByExampleExecutor<SolicitudEntity> {
}
