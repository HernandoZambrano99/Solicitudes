package co.com.bancolombia.model.resultado;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ResultadoSolicitud {
    private Integer solicitudId;
    private String decision;
    private String mensaje;
}
