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
    private String estado;
    private String tipoPrestamo;
    private BigDecimal monto;
    private Integer plazo;
    private String email;
}
