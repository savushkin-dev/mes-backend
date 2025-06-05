package com.host.SpringBootAutomationProduction.model.postgres;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "BD_REPORT")
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "REPORT_NAME")
    private String reportName;

    @Column(name = "REPORT_CATEGORY")
    private String reportCategory;

    @Column(name = "DB_URL")
    private String dbUrl;

    @Column(name = "DB_USERNAME")
    private String dbUsername;

    @Column(name = "DB_PASSWORD")
    private String dbPassword;

    @Column(name = "DB_DRIVER")
    private String dbDriver;

    @Column(name = "SQL")
    private String sql;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "STYLES")
    private String styles;

    @Column(name = "PARAMETERS")
    private String parameters;

}
