package co.com.bancolombia.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRequestDto {
    private Integer tipoPrestamoId;
    private Double monto;
    private Integer plazo;
    private String email;
    private String documentoIdentidad;
}