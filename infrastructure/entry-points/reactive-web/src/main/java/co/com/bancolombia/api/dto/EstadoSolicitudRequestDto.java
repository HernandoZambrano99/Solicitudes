package co.com.bancolombia.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoSolicitudRequestDto {
    private String nuevoEstado; // "APROBADO" o "RECHAZADO"
}
