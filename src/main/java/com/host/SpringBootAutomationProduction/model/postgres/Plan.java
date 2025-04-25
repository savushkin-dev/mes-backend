package com.host.SpringBootAutomationProduction.model.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "BD_PLAN")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "PLAN_ID")
    private String planId;

    @Column(name = "DATA")
    private String data;

    @Override
    public String toString() {
        return "Plan{" +
                "id=" + id +
                ", plan_id='" + planId + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

    public void setId(Object o) { ////временно, потом решить проблему с OptimisticLocking
        this.id = 0;
    }
}
