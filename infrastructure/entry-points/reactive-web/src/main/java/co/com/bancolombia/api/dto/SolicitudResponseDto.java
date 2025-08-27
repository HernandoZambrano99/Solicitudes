package co.com.bancolombia.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitudResponseDto {
    private Integer idSolicitud;
    private String documentoIdentidad;
    private String estado;
    private String tipoPrestamo;
    private Double monto;
    private Integer plazo;
    private String email;
}
