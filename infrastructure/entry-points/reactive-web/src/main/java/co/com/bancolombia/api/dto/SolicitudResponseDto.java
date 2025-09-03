package co.com.bancolombia.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitudResponseDto {
    private Integer idSolicitud;
    private String usuario;
    private String documentoIdentidad;
    private String email;
    private String tipoPrestamo;
    private String estado;
    private BigDecimal monto;
    private Integer plazo;
    private Double tasaInteres;
    private Double deudaTotalMensual;
}
