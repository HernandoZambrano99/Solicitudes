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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

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
        Solicitud solicitud = Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("12345")
                .monto(BigDecimal.valueOf(1_000_000))
                .email("email@example.com")
                .plazo(12)
                .idTipoPrestamo(1)
                .idEstado(1)
                .build();

        Estados estado = Estados.builder()
                .idEstado(1)
                .nombre("Pendiente de revisión")
                .descripcion("Descripción de Prueba")
                .build();

        TipoPrestamo tipoPrestamo = TipoPrestamo.builder()
                .idTipoPrestamo(1)
                .nombre("Prestamo Personal")
                .montoMinimo(BigDecimal.valueOf(100000))
                .montoMaximo(BigDecimal.valueOf(200000000))
                .tasaInteres(15.0)
                .build();

        User user = User.builder()
                .name("Juan")
                .lastName("Pérez")
                .build();

        Solicitud savedSolicitud = Solicitud.builder()
                .idSolicitud(1)
                .monto(BigDecimal.valueOf(1_000_000))
                .email("email@example.com")
                .plazo(12)
                .idTipoPrestamo(1)
                .idEstado(1)
                .documentoIdentidad("12345")
                .build();

        when(validarUsuarioUseCase.validarSiExiste("12345")).thenReturn(Mono.just(user));
        when(tipoPrestamoRepository.findById(eq(1))).thenReturn(Mono.just(tipoPrestamo));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(savedSolicitud));
        when(estadosRepository.findById(eq(1))).thenReturn(Mono.just(estado));

        Mono<SolicitudDetalle> result = solicitudUseCase.ejecutar(solicitud);

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
        Solicitud solicitud = Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("12345")
                .monto(BigDecimal.valueOf(10))
                .plazo(12)
                .idTipoPrestamo(1)
                .build();

        TipoPrestamo tipoPrestamo = TipoPrestamo.builder()
                .idTipoPrestamo(1)
                .nombre("Prestamo Personal")
                .montoMinimo(BigDecimal.valueOf(100000))
                .montoMaximo(BigDecimal.valueOf(200000))
                .build();

        when(validarUsuarioUseCase.validarSiExiste("12345")).thenReturn(Mono.just(User.builder().build()));
        when(tipoPrestamoRepository.findById(eq(1))).thenReturn(Mono.just(tipoPrestamo));

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud))
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
        Solicitud solicitud = Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("12345")
                .monto(BigDecimal.valueOf(500000))
                .plazo(12)
                .idTipoPrestamo(99)
                .idEstado(1)
                .build();

        when(validarUsuarioUseCase.validarSiExiste("12345")).thenReturn(Mono.just(User.builder().build()));
        when(tipoPrestamoRepository.findById(eq(99))).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud))
                .expectErrorMatches(throwable -> throwable instanceof TipoPrestamoNotFoundException &&
                        ((TipoPrestamoNotFoundException) throwable).getMessage().contains("99"))
                .verify();
    }

    @Test
    void saveRequestFailsWhenUsuarioNotFound() {
        Solicitud solicitud = Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("0000")
                .monto(BigDecimal.valueOf(500000))
                .plazo(12)
                .idTipoPrestamo(1)
                .build();

        when(validarUsuarioUseCase.validarSiExiste("0000")).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud))
                .expectErrorMatches(throwable -> throwable instanceof UsuarioNotFoundException &&
                        throwable.getMessage().contains("0000"))
                .verify();
    }


}
