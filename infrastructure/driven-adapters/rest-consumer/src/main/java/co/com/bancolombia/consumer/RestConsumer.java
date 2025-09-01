package co.com.bancolombia.consumer;

import co.com.bancolombia.consumer.exception.SolicitudSoloClienteException;
import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.model.usuario.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RestConsumer implements UserRepository {
    private final WebClient client;

    @Override
    public Mono<User> findByDocument(String identityDocument, String jwt) {
        return client
                .get()
                .uri("/api/v1/usuarios/find/{identityDocument}", identityDocument)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt))
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.empty())
                .bodyToMono(UserResponse.class)
                .map(resp -> User.builder()
                        .id(resp.getId())
                        .name(resp.getName())
                        .lastName(resp.getLastName())
                        .identityDocument(resp.getIdentityDocument())
                        .phone(resp.getPhone())
                        .birthday(resp.getBirthday())
                        .build()
                );
    }

    @Override
    public Mono<Boolean> validateByDocument(String identityDocument, String jwt) {
        return client
                .get()
                .uri("/api/v1/usuarios/validate/{identityDocument}", identityDocument)
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt))
                .retrieve()
                .onStatus(status -> status.value() == 403,
                        response -> Mono.error(new SolicitudSoloClienteException(
                                "Solo los clientes pueden realizar solicitudes de crédito")))
                .onStatus(status -> status.value() == 404,
                        response -> Mono.empty())
                .bodyToMono(ValidateResponse.class)
                .map(ValidateResponse::isMatch)
                .defaultIfEmpty(false);
    }
}
