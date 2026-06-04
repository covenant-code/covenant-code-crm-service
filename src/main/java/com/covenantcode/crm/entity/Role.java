package com.covenantcode.crm.entity;


import com.covenantcode.crm.entity.enums.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    RoleName name;
    private RoleName name;
}
