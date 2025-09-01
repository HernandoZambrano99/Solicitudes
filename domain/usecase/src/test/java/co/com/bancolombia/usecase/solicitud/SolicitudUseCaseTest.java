package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.estados.gateways.EstadosRepository;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.bancolombia.model.usuario.User;
import co.com.bancolombia.usecase.ValidarUsuarioUseCase;
import co.com.bancolombia.usecase.exceptions.MontoFueraDeRangoException;
import co.com.bancolombia.usecase.exceptions.SolicitudNotFoundException;
import co.com.bancolombia.usecase.exceptions.TipoPrestamoNotFoundException;
import co.com.bancolombia.usecase.exceptions.UsuarioNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SolicitudUseCaseTest {

    private SolicitudRepository solicitudRepository;
    private TipoPrestamoRepository tipoPrestamoRepository;
    private EstadosRepository estadosRepository;
    private SolicitudUseCase solicitudUseCase;
    private ValidarUsuarioUseCase validarUsuarioUseCase;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        tipoPrestamoRepository = Mockito.mock(TipoPrestamoRepository.class);
        estadosRepository = Mockito.mock(EstadosRepository.class);
        validarUsuarioUseCase = Mockito.mock(ValidarUsuarioUseCase.class);
        solicitudUseCase = new SolicitudUseCase(solicitudRepository, tipoPrestamoRepository, estadosRepository, validarUsuarioUseCase);
    }

    @Test
    void saveRequestSuccess() {
        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        Estados estado = SolicitudTestData.buildEstadoPendiente();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        User user = SolicitudTestData.buildUsuarioValido();
        Solicitud savedSolicitud = SolicitudTestData.buildSolicitudValida();

        when(validarUsuarioUseCase.validarSiExiste("12345", )).thenReturn(Mono.just(user));
        when(tipoPrestamoRepository.findById(eq(1))).thenReturn(Mono.just(tipoPrestamo));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(savedSolicitud));
        when(estadosRepository.findById(eq(1))).thenReturn(Mono.just(estado));

        Mono<SolicitudDetalle> result = solicitudUseCase.ejecutar(solicitud, );

        StepVerifier.create(result)
                .assertNext(detalle -> {
                    assertEquals(1, detalle.getSolicitud().getIdSolicitud());
                    assertEquals("Pendiente de revisión", detalle.getEstado().getNombre());
                    assertEquals("Prestamo Personal", detalle.getTipoPrestamo().getNombre());
                    assertEquals("email@example.com", detalle.getSolicitud().getEmail());
                    assertEquals("Juan", detalle.getUser().getName());
                })
                .verifyComplete();
    }

    @Test
    void saveRequestFailsWhenMontoFueraDeRango() {
        Solicitud solicitud = SolicitudTestData.buildSolicitudMontoInvalido();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoConRangoCorto();

        when(validarUsuarioUseCase.validarSiExiste("12345", )).thenReturn(Mono.just(User.builder().build()));
        when(tipoPrestamoRepository.findById(eq(1))).thenReturn(Mono.just(tipoPrestamo));

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud, ))
                .expectError(MontoFueraDeRangoException.class)
                .verify();
    }

    @Test
    void buscarPorIdFailsWhenSolicitudNotFound() {
        when(solicitudRepository.findById(eq(999))).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.buscarPorId(999))
                .expectErrorMatches(throwable -> throwable instanceof SolicitudNotFoundException &&
                        throwable.getMessage().contains("999"))
                .verify();
    }

    @Test
    void saveRequestFailsWhenTipoPrestamoNotFound() {
        Solicitud solicitud = SolicitudTestData.buildSolicitudTipoPrestamoInvalido();

        when(validarUsuarioUseCase.validarSiExiste("12345", )).thenReturn(Mono.just(User.builder().build()));
        when(tipoPrestamoRepository.findById(eq(99))).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud, ))
                .expectErrorMatches(throwable -> throwable instanceof TipoPrestamoNotFoundException &&
                        ((TipoPrestamoNotFoundException) throwable).getMessage().contains("99"))
                .verify();
    }

    @Test
    void saveRequestFailsWhenUsuarioNotFound() {
        Solicitud solicitud = SolicitudTestData.buildSolicitudUsuarioInvalido();

        when(validarUsuarioUseCase.validarSiExiste("0000", )).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud, ))
                .expectErrorMatches(throwable -> throwable instanceof UsuarioNotFoundException &&
                        throwable.getMessage().contains("0000"))
                .verify();
    }


}
