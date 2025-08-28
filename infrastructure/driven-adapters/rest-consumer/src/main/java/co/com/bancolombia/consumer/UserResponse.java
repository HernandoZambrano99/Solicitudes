package co.com.bancolombia.consumer;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserResponse {
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