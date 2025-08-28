package co.com.bancolombia.consumer;

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
    public Mono<User> findByDocument(String identityDocument) {
        return client
                .get()
                .uri("/api/v1/usuarios/find/{identityDocument}", identityDocument)
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
}
