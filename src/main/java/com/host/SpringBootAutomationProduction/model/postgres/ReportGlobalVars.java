package com.host.SpringBootAutomationProduction.model.postgres;

import jakarta.persistence.*;
import lombok.*;


@Data
@Entity
@Table(name = "BD_REPORT_GLOBALS")
public class ReportGlobalVars {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "KEY")
    private String key;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "DESCRIPTION")
    private String description;

}
