package co.com.bancolombia.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("tipo_prestamo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TipoPrestamoEntity {
    @Id
    private Integer idTipoPrestamo;
    private String nombre;
    private Double montoMinimo;
    private Double montoMaximo;
    private Double tasaInteres;
    private Boolean validacionAutomatica;
}