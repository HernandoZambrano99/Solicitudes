package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.entity.SolicitudEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SolicitudDataRepository extends ReactiveCrudRepository<SolicitudEntity, Integer>, ReactiveQueryByExampleExecutor<SolicitudEntity> {

    @Query("SELECT * FROM solicitud s WHERE s.id_estado = :idEstado")
    Flux<SolicitudEntity> findSolicitudesParaRevision(@Param("idEstado") Integer idEstado);
}
