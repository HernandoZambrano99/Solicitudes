package co.com.bancolombia.model.estados;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Estados {
    private Integer idEstado;
    private String nombre;
    private String descripcion;
}
