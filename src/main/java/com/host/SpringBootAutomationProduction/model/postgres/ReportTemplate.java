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
@Table(name = "BD_REPORT")
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "REPORT_NAME")
    private String reportName;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "STYLES")
    private String styles;

}
