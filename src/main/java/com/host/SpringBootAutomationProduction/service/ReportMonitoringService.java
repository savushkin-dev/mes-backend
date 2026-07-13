package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.postgres.ReportAccessLog;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportAccessLogRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@Service
public class ReportMonitoringService {

    private final ReportAccessLogRepository logRepository;

    @Autowired
    public ReportMonitoringService(ReportAccessLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Логирует успешный доступ к отчету (асинхронно)
     */
    @Async
    @Transactional
    public void logReportAccess(String category, String reportName) {
        ReportAccessLog reportLog = new ReportAccessLog();
        reportLog.setCategory(category);
        reportLog.setReportName(reportName);
        reportLog.setAccessTime(LocalDateTime.now());

        logRepository.save(reportLog);

        log.debug("Report access logged: {} / {}", category, reportName);
    }

    /**
     * Получить события за период
     */
    public List<ReportAccessLog> getEventsByPeriod(LocalDateTime from, LocalDateTime to) {
        return logRepository.findByAccessTimeBetweenOrderByAccessTimeDesc(from, to);
    }


}