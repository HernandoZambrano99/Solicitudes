package co.com.bancolombia.model;

import co.com.bancolombia.model.estados.Estados;
import co.com.bancolombia.model.solicitud.Solicitud;
import co.com.bancolombia.model.tipoprestamo.PrestamoConTipo;
import co.com.bancolombia.model.tipoprestamo.TipoPrestamo;
import co.com.bancolombia.model.usuario.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SolicitudDetalle {
    private Solicitud solicitud;
    private Estados estado;
    private TipoPrestamo tipoPrestamo;
    private User user;
    private List<PrestamoConTipo> prestamosAprobados;
}
