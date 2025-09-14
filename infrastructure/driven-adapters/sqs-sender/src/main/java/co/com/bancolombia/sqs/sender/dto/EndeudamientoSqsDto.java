package co.com.bancolombia.sqs.sender.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EndeudamientoSqsDto {

    // Identificación de la solicitud
    private Integer solicitudId;

    // Datos del usuario
    private Long usuarioId;
    private String nombreUsuario;
    private String emailUsuario;
    private BigDecimal ingresosTotales; // salario u otros ingresos
    private String documentoIdentidad;

    // Nuevo préstamo
    private BigDecimal montoNuevoPrestamo;
    private Double tasaInteresAnualNuevo; // % anual
    private Integer plazoMesesNuevo;
    private Integer idTipoPrestamo;
    private String nombreTipoPrestamo;
    private Boolean validacionAutomatica;

    // Lista de préstamos activos aprobados
    private List<PrestamoActivoDto> prestamosActivos;

    // Salario mínimo actual (opcional)
    private BigDecimal salarioMinimoActual;
}