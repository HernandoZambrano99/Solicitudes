package co.com.bancolombia.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResponseDto {
    private Integer idSolicitud;
    private String documentoIdentidad;
    private String estado;
    private String tipoPrestamo;
    private Double monto;
    private Integer plazo;
    private String email;
}
