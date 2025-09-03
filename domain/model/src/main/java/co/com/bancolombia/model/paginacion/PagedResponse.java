package co.com.bancolombia.model.paginacion;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PagedResponse<T> {
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalRecords;
    private Integer totalPages;
    private List<T> data;
}
