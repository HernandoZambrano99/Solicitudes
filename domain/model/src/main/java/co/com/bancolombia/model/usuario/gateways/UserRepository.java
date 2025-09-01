package co.com.bancolombia.model.usuario.gateways;

import co.com.bancolombia.model.usuario.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> findByDocument(String identityDocument, String jwt);
    Mono<Boolean> validateByDocument(String identityDocument, String jwt);
}
