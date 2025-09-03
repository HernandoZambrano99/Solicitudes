package co.com.bancolombia.r2dbc.transactional;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.paginacion.PagedResponse;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SolicitudTransactionalAdapter {

    private final SolicitudUseCase solicitudUseCase;
    private final TransactionalOperator transactionalOperator;

    public Mono<SolicitudDetalle> crearSolicitud(Solicitud solicitud){
        return solicitudUseCase.ejecutar(solicitud, null )
                .as(transactionalOperator::transactional);
    }

    public Mono<Solicitud> buscarPorId(Integer id) {
        return solicitudUseCase.buscarPorId(id)
                .as(transactionalOperator::transactional);
    }

    public Mono<PagedResponse<SolicitudDetalle>> buscarPorFiltro(Integer idEstado, PageRequest pageRequest) {
        return solicitudUseCase.getSolicitudesByEstado(idEstado, pageRequest)
                .as(transactionalOperator::transactional);
    }
}
