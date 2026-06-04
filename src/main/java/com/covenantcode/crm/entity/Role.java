package com.covenantcode.crm.entity;


import com.covenantcode.crm.entity.enums.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    RoleName name;
}
