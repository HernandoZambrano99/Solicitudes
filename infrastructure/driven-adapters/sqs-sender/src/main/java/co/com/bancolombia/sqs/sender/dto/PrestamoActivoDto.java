package co.com.bancolombia.sqs.sender.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PrestamoActivoDto {
    private BigDecimal monto;
    private Double tasaInteresAnual; // % anual
    private Integer plazoMeses;
    private Integer mesesRestantes;
    private String estado; // Aprobado, etc.
}