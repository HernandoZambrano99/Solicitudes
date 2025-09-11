package co.com.bancolombia.r2dbc.transactional;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.paginacion.PagedResponse;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.usecase.solicitud.SolicitudUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudTransactionalAdapter {

    private final SolicitudUseCase solicitudUseCase;
    private final TransactionalOperator transactionalOperator;

    public Mono<SolicitudDetalle> crearSolicitud(Solicitud solicitud){
        return solicitudUseCase.ejecutar(solicitud)
                .as(transactionalOperator::transactional);
    }

    public Mono<Solicitud> buscarPorId(Integer id) {
        return solicitudUseCase.buscarPorId(id)
                .as(transactionalOperator::transactional);
    }

    public Mono<PagedResponse<SolicitudDetalle>> buscarPorFiltro(List<Integer> idEstados, PageRequest pageRequest) {
        return solicitudUseCase.getSolicitudesByEstado(idEstados, pageRequest)
                .as(transactionalOperator::transactional);
    }
}
