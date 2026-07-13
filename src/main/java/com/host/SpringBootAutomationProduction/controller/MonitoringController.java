package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.model.postgres.ReportAccessLog;
import com.host.SpringBootAutomationProduction.service.ReportMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final ReportMonitoringService monitoringService;

    @Autowired
    public MonitoringController(ReportMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Получить события просмотра отчетов за период
     * Пример: GET /api/monitoring/reports/events?from=2024-01-15T00:00:00&to=2024-01-15T23:59:59
     */
    @GetMapping("/reports/events")
    public ResponseEntity<List<ReportAccessLog>> getReportEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<ReportAccessLog> events = monitoringService.getEventsByPeriod(from, to);
        return ResponseEntity.ok(events);
    }

}
