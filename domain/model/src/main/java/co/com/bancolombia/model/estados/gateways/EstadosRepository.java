package co.com.bancolombia.model.estados.gateways;

import co.com.bancolombia.model.estados.Estados;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface EstadosRepository {
    Flux<Estados> findAll();
    Mono<Estados> findById(Integer id);
}