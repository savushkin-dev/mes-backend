package com.host.SpringBootAutomationProduction.model.postgres;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "BD_ROLES")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "F_ID")
    private int F_ID;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name; // ROLE_ADMIN, ROLE_USER


    @ManyToMany(mappedBy = "roles")
    private Set<User> users;


}