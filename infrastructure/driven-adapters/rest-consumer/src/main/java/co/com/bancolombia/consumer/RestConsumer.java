package co.com.bancolombia.consumer;

import co.com.bancolombia.consumer.exception.SolicitudSoloClienteException;
import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.model.usuario.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestConsumer implements UserRepository {
    private final WebClient client;

    @Override
    public Mono<User> findByDocument(String identityDocument) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnSubscribe(s -> log.info("Buscando token en contexto"))
                .map(ctx -> (String) ctx.getAuthentication().getCredentials())
                .doOnNext(jwt -> log.info("JWT encontrado {}", jwt))
                .flatMap(jwt -> client.get()
                        .uri("/api/v1/usuarios/find/{identityDocument}", identityDocument)
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt))
                        .exchangeToMono(response -> {
                            log.info("Respuesta {} para usuario {}", response.statusCode(), identityDocument);
                            if (response.statusCode().is2xxSuccessful()) {
                                return response.bodyToMono(UserResponse.class)
                                        .map(resp -> User.builder()
                                                .id(resp.getId())
                                                .name(resp.getName())
                                                .lastName(resp.getLastName())
                                                .identityDocument(resp.getIdentityDocument())
                                                .phone(resp.getPhone())
                                                .birthday(resp.getBirthday())
                                                .email(resp.getEmail())
                                                .salary(resp.getSalary())
                                                .address(resp.getAddress())
                                                .build());
                            } else if (response.statusCode().value() == 404) {
                                return Mono.empty();
                            } else {
                                return response.createException().flatMap(Mono::error);
                            }
                        })
                );
    }

    @Override
    public Mono<Boolean> validateByDocument(String identityDocument) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (String) ctx.getAuthentication().getCredentials())
                .flatMap(jwt -> client.get()
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
                        .defaultIfEmpty(false)
                );
    }
}
