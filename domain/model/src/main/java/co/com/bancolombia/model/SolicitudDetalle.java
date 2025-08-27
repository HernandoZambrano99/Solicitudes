package co.com.bancolombia.model;

import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SolicitudDetalle {
    private Solicitud solicitud;
    private Estados estado;
    private TipoPrestamo tipoPrestamo;
}
