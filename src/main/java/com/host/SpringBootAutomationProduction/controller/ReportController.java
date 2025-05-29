package com.host.SpringBootAutomationProduction.controller;


import com.host.SpringBootAutomationProduction.dto.ReportTemplateDTO;
import com.host.SpringBootAutomationProduction.model.LuMove;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.service.LuMoveService;
import com.host.SpringBootAutomationProduction.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;
    private final LuMoveService luMoveService;


    @Autowired
    public ReportController(ReportService reportService, LuMoveService luMoveService) {
        this.reportService = reportService;
        this.luMoveService = luMoveService;
    }


    @GetMapping("/{reportName}")
    public ReportTemplateDTO getReportByName(@PathVariable("reportName") String reportName) {
        log.info("Received request '/{reportName}': {}", reportName);
        return convertToReportTemplateDTO(reportService.findByReportName(reportName)).encrypt();
    }

    @GetMapping("/names")
    public ResponseEntity<?> getReportNameList() {
        return ResponseEntity.ok(reportService.findAllReportName());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getGroupedReports() {
        return ResponseEntity.ok(reportService.getReportsNameGroupedByCategory());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrUpdateReport(@RequestBody ReportTemplateDTO reportTemplateDTO) {
        log.info("Received request '/create': {}", reportTemplateDTO);
        ReportTemplate reportTemplate = convertToReportTemplate(reportTemplateDTO.decrypt());
        reportService.saveOrUpdateReport(reportTemplate);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/data/{reportName}")
    public ResponseEntity<?> getDataByReportName(@PathVariable("reportName") String reportName) {
        log.info("Received request '/data/{reportName}': {}", reportName);
        return ResponseEntity.ok(reportService.getDataByReportName(reportName));
    }

    @PostMapping("/data")
    public ResponseEntity<?> getDataForReport(@RequestBody ReportTemplateDTO reportTemplateDTO) {
        log.info("Received request '/data': {}", reportTemplateDTO);
        ReportTemplate reportTemplate = convertToReportTemplate(reportTemplateDTO.decrypt());
        return ResponseEntity.ok(reportService.getDataForReport(reportTemplate));
    }


    private ReportTemplate convertToReportTemplate(ReportTemplateDTO reportTemplateDTO) {
        return new ModelMapper().map(reportTemplateDTO, ReportTemplate.class);
    }

    private ReportTemplateDTO convertToReportTemplateDTO(ReportTemplate reportTemplate) {
        return new ModelMapper().map(reportTemplate, ReportTemplateDTO.class);
    }

    @GetMapping("/lumoveday")
    public ResponseEntity<?> getLuMoveDay(){
        List<LuMove> luMoveList = luMoveService.getLuMoveDay();
        return ResponseEntity.ok(luMoveList);
    }



}
