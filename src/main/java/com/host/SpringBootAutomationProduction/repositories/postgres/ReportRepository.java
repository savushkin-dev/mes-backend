package com.host.SpringBootAutomationProduction.repositories.postgres;

import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<ReportTemplate, Integer> {

    Optional<ReportTemplate> findByReportName(String reportName);


}
