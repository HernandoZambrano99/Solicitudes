package co.com.bancolombia.usecase.solicitud;

import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.usuario.User;

import java.math.BigDecimal;

public class SolicitudTestData {
    public static Solicitud buildSolicitudValida() {
        return Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("12345")
                .monto(BigDecimal.valueOf(1_000_000))
                .email("email@example.com")
                .plazo(12)
                .idTipoPrestamo(1)
                .idEstado(1)
                .build();
    }

    public static Solicitud buildSolicitudMontoInvalido() {
        return Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("12345")
                .monto(BigDecimal.valueOf(10)) // fuera de rango
                .plazo(12)
                .idTipoPrestamo(1)
                .build();
    }

    public static Solicitud buildSolicitudTipoPrestamoInvalido() {
        return Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("12345")
                .monto(BigDecimal.valueOf(500000))
                .plazo(12)
                .idTipoPrestamo(99) // no existe
                .idEstado(1)
                .build();
    }

    public static Solicitud buildSolicitudUsuarioInvalido() {
        return Solicitud.builder()
                .idSolicitud(1)
                .documentoIdentidad("0000")
                .monto(BigDecimal.valueOf(500000))
                .plazo(12)
                .idTipoPrestamo(1)
                .build();
    }

    public static Estados buildEstadoPendiente() {
        return Estados.builder()
                .idEstado(1)
                .nombre("Pendiente de revisión")
                .descripcion("Descripción de Prueba")
                .build();
    }

    public static TipoPrestamo buildTipoPrestamoValido() {
        return TipoPrestamo.builder()
                .idTipoPrestamo(1)
                .nombre("Prestamo Personal")
                .montoMinimo(BigDecimal.valueOf(100000))
                .montoMaximo(BigDecimal.valueOf(200000000))
                .tasaInteres(15.0)
                .build();
    }

    public static TipoPrestamo buildTipoPrestamoConRangoCorto() {
        return TipoPrestamo.builder()
                .idTipoPrestamo(1)
                .nombre("Prestamo Personal")
                .montoMinimo(BigDecimal.valueOf(100000))
                .montoMaximo(BigDecimal.valueOf(200000))
                .build();
    }

    public static User buildUsuarioValido() {
        return User.builder()
                .name("Juan")
                .lastName("Pérez")
                .build();
    }

    public static User buildUsuarioBasico() {
        return User.builder().build();
    }
}