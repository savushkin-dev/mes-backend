package com.host.SpringBootAutomationProduction.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class ReportMonitoringService {

    private final Queue<ReportAccessEvent> events = new ConcurrentLinkedQueue<>();
    private static final int MAX_EVENTS = 10000;

    @Data
    @AllArgsConstructor
    public static class ReportAccessEvent {
        private String category;
        private String reportName;
        private LocalDateTime timestamp;
    }

    /**
     * Логирует успешный доступ к отчету
     */
    public void logReportAccess(String category, String reportName) {
        events.offer(new ReportAccessEvent(category, reportName, LocalDateTime.now()));

        // Ограничиваем размер очереди
        while (events.size() > MAX_EVENTS) {
            events.poll();
        }

        log.debug("Report accessed: {} / {}", category, reportName);
    }

    /**
     * Получить события за период
     */
    public List<ReportAccessEvent> getEventsByPeriod(LocalDateTime from, LocalDateTime to) {
        return events.stream()
                .filter(e -> isBetween(e.getTimestamp(), from, to))
                .toList();
    }

    /**
     * Проверяет, находится ли timestamp в указанном периоде
     */
    private boolean isBetween(LocalDateTime timestamp, LocalDateTime from, LocalDateTime to) {
        return !timestamp.isBefore(from) && !timestamp.isAfter(to);
    }

}