package co.com.bancolombia.usecase;

import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.model.usuario.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ValidarUsuarioUseCase {

    private final UserRepository userRepository;

    public Mono<User> validarSiExiste(String identityDocument, String jwt) {
        return userRepository.findByDocument(identityDocument, jwt );
    }

    public Mono<Boolean> validarSiCoincideConJwt(String identityDocument, String jwt) {
        return userRepository.validateByDocument(identityDocument, jwt );
    }
}