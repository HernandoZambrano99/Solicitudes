package co.com.bancolombia.model.usuario;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private Long id;
    private String name;
    private String lastName;
    private String identityDocument;
    private LocalDateTime birthday;
    private String address;
    private Integer phone;
    private String email;
    private BigDecimal salary;

}
