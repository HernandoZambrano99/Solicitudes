package co.com.bancolombia.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRequestDto {
    @NotNull(message = "El tipo de prestamo es obligatorio")
    @Positive(message = "El tipo de prestamo debe ser valido")
    private Integer tipoPrestamoId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "El plazo es obligatorio")
    private Integer plazo;

    @Email(message = "Formato de correo no válido")
    private String email;

    @NotBlank(message = "El documento es obligatorio")
    private String documentoIdentidad;
}