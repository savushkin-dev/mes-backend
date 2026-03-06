package com.host.SpringBootAutomationProduction.repositories.postgres;

import com.host.SpringBootAutomationProduction.model.postgres.ReportGlobalVars;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportGlobalVarsRepository extends JpaRepository<ReportGlobalVars, Long> {

    List<ReportGlobalVars> findAllByOrderByKeyAsc();
}
