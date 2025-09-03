package co.com.bancolombia.model.paginacion;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SolicitudFilters {
    private String status;
    private String email;
    private String name;
    private Double minAmount;
    private Double maxAmount;
    private String applicationStatus;
}