package co.com.bancolombia.model.tipoprestamo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TipoPrestamo {
    private Integer idTipoPrestamo;
    private String nombre;
    private Double montoMinimo;
    private Double montoMaximo;
    private Double tasaInteres;
    private Boolean validacionAutomatica;
}
