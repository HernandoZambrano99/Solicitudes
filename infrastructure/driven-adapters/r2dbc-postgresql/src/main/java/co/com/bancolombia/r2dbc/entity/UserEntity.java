package co.com.bancolombia.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("last_name")
    private String lastName;

    @Column("birthday")
    private LocalDateTime birthday;

    @Column("address")
    private String address;

    @Column("phone")
    private Integer phone;

    @Column("email")
    private String email;

    @Column("salary")
    private BigDecimal salary;

    @Column("identity_document")
    private String identityDocument;

    @Column("password")
    private String password;

    @Column("id_rol")
    private Long roleId;
}
