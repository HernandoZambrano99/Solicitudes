package co.com.bancolombia.model.paginacion;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PageRequest {
    @Builder.Default
    private Integer pageSize = 10;

    @Builder.Default
    private Integer pageNumber = 1;
}
