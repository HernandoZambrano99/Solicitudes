package co.com.bancolombia.model.sqs.gateways;

import co.com.bancolombia.model.SolicitudDetalle;
import reactor.core.publisher.Mono;

public interface SqsGateway {
    Mono<Void> enviarSolicitudActualizada(SolicitudDetalle detalle);
}
