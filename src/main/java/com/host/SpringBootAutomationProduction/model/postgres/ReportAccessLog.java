package com.host.SpringBootAutomationProduction.model.postgres;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "REPORT_ACCESS_LOG")
public class ReportAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CATEGORY", nullable = false)
    private String category;

    @Column(name = "REPORT_NAME", nullable = false)
    private String reportName;

    @Column(name = "ACCESS_TIME", nullable = false)
    private LocalDateTime accessTime;

}
