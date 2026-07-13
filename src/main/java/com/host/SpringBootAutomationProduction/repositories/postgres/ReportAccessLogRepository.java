package com.host.SpringBootAutomationProduction.repositories.postgres;

import com.host.SpringBootAutomationProduction.model.postgres.ReportAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportAccessLogRepository extends JpaRepository<ReportAccessLog, Long> {

    List<ReportAccessLog> findByAccessTimeBetweenOrderByAccessTimeDesc(LocalDateTime from, LocalDateTime to);

}