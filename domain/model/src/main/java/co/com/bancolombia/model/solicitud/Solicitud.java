package co.com.bancolombia.model.solicitud;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private Integer idSolicitud;
    private Double monto;
    private Integer plazo;
    private String email;
    private String documentoIdentidad;
    private Integer idEstado;
    private Integer idTipoPrestamo;
}
