package co.com.bancolombia.sqs.sender.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudesSqsDto {
    private Integer solicitudId;
    private String estado;
    private String correoCliente;
    private String nombreCliente;
    private String tipoPrestamo;
    private Integer plazo;
    private BigDecimal monto;
}
