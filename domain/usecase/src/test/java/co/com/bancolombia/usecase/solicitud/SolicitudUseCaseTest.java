package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.SolicitudDetalle;
import co.com.bancolombia.model.resultado.ResultadoSolicitud;
import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.estados.gateways.EstadosRepository;
import co.com.bancolombia.model.paginacion.PageRequest;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.solicitud.gateways.SolicitudRepository;
import co.com.bancolombia.model.sqs.gateways.CapacidadSqsGateway;
import co.com.bancolombia.model.sqs.gateways.SqsGateway;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class SolicitudUseCaseTest {

    private SolicitudRepository solicitudRepository;
    private TipoPrestamoRepository tipoPrestamoRepository;
    private EstadosRepository estadosRepository;
    private SolicitudUseCase solicitudUseCase;
    private ValidarUsuarioUseCase validarUsuarioUseCase;
    private SqsGateway sqsGateway;
    private CapacidadSqsGateway capacidadSqsGateway;

    @BeforeEach
    void setUp() {
        solicitudRepository = Mockito.mock(SolicitudRepository.class);
        tipoPrestamoRepository = Mockito.mock(TipoPrestamoRepository.class);
        estadosRepository = Mockito.mock(EstadosRepository.class);
        validarUsuarioUseCase = Mockito.mock(ValidarUsuarioUseCase.class);
        sqsGateway = Mockito.mock(SqsGateway.class);
        capacidadSqsGateway = Mockito.mock(CapacidadSqsGateway.class);
        solicitudUseCase = new SolicitudUseCase(solicitudRepository, tipoPrestamoRepository, estadosRepository, validarUsuarioUseCase, sqsGateway, capacidadSqsGateway);
    }

    @Test
    void saveRequestSuccess() {
        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        Estados estado = SolicitudTestData.buildEstadoPendiente();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        User user = SolicitudTestData.buildUsuarioValido();
        Solicitud savedSolicitud = SolicitudTestData.buildSolicitudValida();
        String identityDocument = "12345";


        when(validarUsuarioUseCase.validarSiExiste(identityDocument)).thenReturn(Mono.just(user));
        when(validarUsuarioUseCase.validarSiCoincideConJwt(identityDocument)).thenReturn(Mono.just(true));
        when(tipoPrestamoRepository.findById(eq(1))).thenReturn(Mono.just(tipoPrestamo));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(savedSolicitud));
        when(estadosRepository.findById(eq(1))).thenReturn(Mono.just(estado));
        when(solicitudRepository.findById(anyInt()))
                .thenReturn(Mono.just(savedSolicitud));
        when(capacidadSqsGateway.consultarCapacidadEndeudamiento(any(SolicitudDetalle.class)))
                .thenReturn(Mono.empty());
        when(solicitudRepository.findSolicitudesAprobadasByUsuario(anyString(), anyInt()))
                .thenReturn(Flux.empty());

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
        Solicitud solicitud = SolicitudTestData.buildSolicitudMontoInvalido();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoConRangoCorto();
        String identityDocument = "12345";

        when(validarUsuarioUseCase.validarSiExiste(identityDocument)).thenReturn(Mono.just(User.builder().build()));
        when(validarUsuarioUseCase.validarSiCoincideConJwt(identityDocument)).thenReturn(Mono.just(true));
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
        Solicitud solicitud = SolicitudTestData.buildSolicitudTipoPrestamoInvalido();
        String identityDocument = "12345";

        when(validarUsuarioUseCase.validarSiExiste("12345")).thenReturn(Mono.just(User.builder().build()));
        when(validarUsuarioUseCase.validarSiCoincideConJwt(identityDocument)).thenReturn(Mono.just(true));
        when(tipoPrestamoRepository.findById(eq(99))).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud))
                .expectErrorMatches(throwable -> throwable instanceof TipoPrestamoNotFoundException &&
                        ((TipoPrestamoNotFoundException) throwable).getMessage().contains("99"))
                .verify();
    }

    @Test
    void saveRequestFailsWhenUsuarioNotFound() {
        Solicitud solicitud = SolicitudTestData.buildSolicitudUsuarioInvalido();
        String identityDocument = "0000";

        when(validarUsuarioUseCase.validarSiExiste(identityDocument)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud))
                .expectErrorMatches(throwable -> throwable instanceof UsuarioNotFoundException &&
                        throwable.getMessage().contains("0000"))
                .verify();
    }

    @Test
    void getSolicitudesByEstadoReturnsEmptyWhenNoRecords() {
        PageRequest pageRequest = PageRequest.builder().pageNumber(1).pageSize(10).build();
        List<Integer> estados = List.of(1, 2);

        when(solicitudRepository.countByIdEstado(estados)).thenReturn(Mono.just(0L));

        StepVerifier.create(solicitudUseCase.getSolicitudesByEstado(estados, pageRequest))
                .assertNext(paged -> {
                    assertEquals(0, paged.getTotalPages());
                    assertEquals(0, paged.getTotalRecords());
                    assertEquals(0, paged.getData().size());
                })
                .verifyComplete();
    }

    @Test
    void getSolicitudesByEstadoReturnsRecords() {
        PageRequest pageRequest = PageRequest.builder().pageNumber(1).pageSize(10).build();
        List<Integer> estados = List.of(1);

        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        Estados estado = SolicitudTestData.buildEstadoPendiente();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        User user = SolicitudTestData.buildUsuarioValido();

        when(solicitudRepository.countByIdEstado(estados)).thenReturn(Mono.just(1L));
        when(solicitudRepository.findByIdEstadoPaged(estados, pageRequest)).thenReturn(reactor.core.publisher.Flux.just(solicitud));
        when(estadosRepository.findById(1)).thenReturn(Mono.just(estado));
        when(tipoPrestamoRepository.findById(1)).thenReturn(Mono.just(tipoPrestamo));
        when(validarUsuarioUseCase.validarSiExiste("12345")).thenReturn(Mono.just(user));

        StepVerifier.create(solicitudUseCase.getSolicitudesByEstado(estados, pageRequest))
                .assertNext(paged -> {
                    assertEquals(1, paged.getTotalPages());
                    assertEquals(1, paged.getTotalRecords());
                    assertEquals(1, paged.getData().size());
                    assertEquals("email@example.com", paged.getData().get(0).getSolicitud().getEmail());
                })
                .verifyComplete();
    }

    @Test
    void saveRequestFailsWhenUsuarioNoCoincide() {
        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        String identityDocument = solicitud.getDocumentoIdentidad();

        when(validarUsuarioUseCase.validarSiExiste(identityDocument))
                .thenReturn(Mono.just(SolicitudTestData.buildUsuarioValido()));
        when(validarUsuarioUseCase.validarSiCoincideConJwt(identityDocument))
                .thenReturn(Mono.just(false));

        StepVerifier.create(solicitudUseCase.ejecutar(solicitud))
                .expectErrorMatches(throwable ->
                        throwable instanceof co.com.bancolombia.usecase.exceptions.UsuarioNoCoincideException &&
                                throwable.getMessage().contains(identityDocument))
                .verify();
    }

    @Test
    void aprobarORechazarSuccess() {
        Integer idSolicitud = 1;
        String nuevoEstado = "APROBADO";

        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        Estados estado = SolicitudTestData.buildEstadoPendiente();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        User user = SolicitudTestData.buildUsuarioValido();

        // mocks
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(solicitud));
        when(estadosRepository.findById(any(Integer.class))).thenReturn(Mono.just(estado));
        when(tipoPrestamoRepository.findById(any(Integer.class))).thenReturn(Mono.just(tipoPrestamo));
        when(validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad()))
                .thenReturn(Mono.just(user));
        when(sqsGateway.enviarSolicitudActualizada(any(SolicitudDetalle.class))).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.aprobarORechazar(idSolicitud, nuevoEstado))
                .assertNext(detalle -> {
                    assertEquals(solicitud, detalle.getSolicitud());
                    assertEquals(estado, detalle.getEstado());
                    assertEquals(tipoPrestamo, detalle.getTipoPrestamo());
                    assertEquals(user, detalle.getUser());
                })
                .verifyComplete();
    }

    @Test
    void aprobarORechazarFailsWhenSolicitudNotFound() {
        Integer idSolicitud = 999;
        String nuevoEstado = "APROBADO";

        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.aprobarORechazar(idSolicitud, nuevoEstado))
                .expectErrorMatches(throwable -> throwable instanceof SolicitudNotFoundException &&
                        throwable.getMessage().contains(idSolicitud.toString()))
                .verify();
    }

    @Test
    void aprobarORechazarFailsWhenEstadoInvalido() {
        Integer idSolicitud = 1;
        String nuevoEstado = "INVALIDO";

        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();

        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.aprobarORechazar(idSolicitud, nuevoEstado))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void aprobarORechazarFailsWhenSQSError() {
        Integer idSolicitud = 1;
        String nuevoEstado = "APROBADO";
        String jwt = "fake-jwt";

        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        Estados estado = SolicitudTestData.buildEstadoPendiente();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        User user = SolicitudTestData.buildUsuarioValido();

        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(solicitud));
        when(estadosRepository.findById(any(Integer.class))).thenReturn(Mono.just(estado));
        when(tipoPrestamoRepository.findById(any(Integer.class))).thenReturn(Mono.just(tipoPrestamo));
        when(validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad()))
                .thenReturn(Mono.just(user));
        when(sqsGateway.enviarSolicitudActualizada(any(SolicitudDetalle.class)))
                .thenReturn(Mono.error(new RuntimeException("SQS fallo")));

        StepVerifier.create(solicitudUseCase.aprobarORechazar(idSolicitud, nuevoEstado))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("SQS fallo"))
                .verify();
    }

    @Test
    void validarYEnviarCapacidadEndeudamientoCallsEnviarWhenValidacionAutomaticaTrue() {
        Integer idSolicitud = 1;

        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        solicitud.setIdSolicitud(idSolicitud);

        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        tipoPrestamo.setValidacionAutomatica(true);

        User user = SolicitudTestData.buildUsuarioValido();
        Estados estado = SolicitudTestData.buildEstadoPendiente();

        // mocks para validarYEnviarCapacidadEndeudamiento
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(solicitud));
        when(tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())).thenReturn(Mono.just(tipoPrestamo));

        // mocks para enviarCapacidadEndeudamiento interno
        when(validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad())).thenReturn(Mono.just(user));
        when(solicitudRepository.findSolicitudesAprobadasByUsuario(any(), any()))
                .thenReturn(Flux.just(solicitud));
        when(estadosRepository.findById(solicitud.getIdEstado())).thenReturn(Mono.just(estado));
        when(tipoPrestamoRepository.findById(any())).thenReturn(Mono.just(tipoPrestamo));
        when(capacidadSqsGateway.consultarCapacidadEndeudamiento(any(SolicitudDetalle.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.validarYEnviarCapacidadEndeudamiento(idSolicitud))
                .verifyComplete();
    }

    @Test
    void validarYEnviarCapacidadEndeudamientoSkipsWhenValidacionAutomaticaFalse() {
        Integer idSolicitud = 1;
        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        tipoPrestamo.setValidacionAutomatica(false);

        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(solicitud));
        when(tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())).thenReturn(Mono.just(tipoPrestamo));

        StepVerifier.create(solicitudUseCase.validarYEnviarCapacidadEndeudamiento(idSolicitud))
                .verifyComplete();
    }

    @Test
    void enviarCapacidadEndeudamientoSuccess() {
        Integer idSolicitud = 1;
        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        User user = SolicitudTestData.buildUsuarioValido();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        Estados estado = SolicitudTestData.buildEstadoPendiente();

        // mocks principales
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(solicitud));
        when(validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad())).thenReturn(Mono.just(user));
        // solicitudes aprobadas del usuario
        when(solicitudRepository.findSolicitudesAprobadasByUsuario(any(), any()))
                .thenReturn(reactor.core.publisher.Flux.just(solicitud));
        when(tipoPrestamoRepository.findById(any())).thenReturn(Mono.just(tipoPrestamo));
        when(estadosRepository.findById(any())).thenReturn(Mono.just(estado));
        when(capacidadSqsGateway.consultarCapacidadEndeudamiento(any(SolicitudDetalle.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.enviarCapacidadEndeudamiento(idSolicitud))
                .verifyComplete();
    }
    @Test
    void actualizarEstadoConResultadoSuccess() {
        ResultadoSolicitud result = new ResultadoSolicitud();
        result.setSolicitudId(1);
        result.setDecision("APROBADO");

        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();

        when(solicitudRepository.findById(1)).thenReturn(Mono.just(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(Mono.just(solicitud));

        StepVerifier.create(solicitudUseCase.actualizarEstadoConResultado(result))
                .verifyComplete();
    }

    @Test
    void actualizarEstadoConResultadoFailsWhenSolicitudNotFound() {
        ResultadoSolicitud result = new ResultadoSolicitud();
        result.setSolicitudId(999);
        result.setDecision("APROBADO");

        when(solicitudRepository.findById(999)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.actualizarEstadoConResultado(result))
                .expectError(SolicitudNotFoundException.class)
                .verify();
    }

    @Test
    void buscarDetallePorIdSuccess() {
        Integer idSolicitud = 1;
        Solicitud solicitud = SolicitudTestData.buildSolicitudValida();
        Estados estado = SolicitudTestData.buildEstadoPendiente();
        TipoPrestamo tipoPrestamo = SolicitudTestData.buildTipoPrestamoValido();
        User user = SolicitudTestData.buildUsuarioValido();

        when(solicitudRepository.findById(idSolicitud)).thenReturn(Mono.just(solicitud));
        when(estadosRepository.findById(solicitud.getIdEstado())).thenReturn(Mono.just(estado));
        when(tipoPrestamoRepository.findById(solicitud.getIdTipoPrestamo())).thenReturn(Mono.just(tipoPrestamo));
        when(validarUsuarioUseCase.validarSiExiste(solicitud.getDocumentoIdentidad())).thenReturn(Mono.just(user));

        StepVerifier.create(solicitudUseCase.buscarDetallePorId(idSolicitud))
                .assertNext(detalle -> {
                    assertEquals(solicitud, detalle.getSolicitud());
                    assertEquals(estado, detalle.getEstado());
                    assertEquals(tipoPrestamo, detalle.getTipoPrestamo());
                    assertEquals(user, detalle.getUser());
                })
                .verifyComplete();
    }

    @Test
    void buscarDetallePorIdFailsWhenNotFound() {
        when(solicitudRepository.findById(999)).thenReturn(Mono.empty());

        StepVerifier.create(solicitudUseCase.buscarDetallePorId(999))
                .expectError(SolicitudNotFoundException.class)
                .verify();
    }


}
