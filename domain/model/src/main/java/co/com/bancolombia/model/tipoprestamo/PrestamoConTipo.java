package co.com.bancolombia.model.tipoprestamo;

import co.com.bancolombia.model.solicitud.Solicitud;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PrestamoConTipo {
    private Solicitud solicitud;
    private TipoPrestamo tipoPrestamo;
}
