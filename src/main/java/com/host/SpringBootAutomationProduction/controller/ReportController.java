package com.host.SpringBootAutomationProduction.controller;


import com.host.SpringBootAutomationProduction.dto.ReportTemplateDTO;
import com.host.SpringBootAutomationProduction.model.LuMove;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.service.DataSourceService;
import com.host.SpringBootAutomationProduction.service.LuMoveService;
import com.host.SpringBootAutomationProduction.service.ReportService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;
    private final LuMoveService luMoveService;

    private final DataSourceService dataSourceService;

    @Autowired
    public ReportController(ReportService reportService, LuMoveService luMoveService, DataSourceService dataSourceService) {
        this.reportService = reportService;
        this.luMoveService = luMoveService;
        this.dataSourceService = dataSourceService;
    }


    @GetMapping("/{reportName}")
    public ReportTemplateDTO getReportByName(@PathVariable("reportName") String reportName) {
        return convertToReportTemplateDTO(reportService.findByReportName(reportName));
    }

    @GetMapping("/reportsName")
    public ResponseEntity<?> getReportNameList() {
        return ResponseEntity.ok(reportService.findAllReportName());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrUpdateReport(@RequestBody ReportTemplateDTO reportTemplateDTO) {
        ReportTemplate reportTemplate = convertToReportTemplate(reportTemplateDTO);
        reportService.saveOrUpdateReport(reportTemplate);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/data/{reportName}")
    public ResponseEntity<?> getDataForReport(@PathVariable("reportName") String reportName) {
        return ResponseEntity.ok(reportService.getDataForReport(reportName));
    }


    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody ReportTemplateDTO reportTemplateDTO){
        ReportTemplate reportTemplate = convertToReportTemplate(reportTemplateDTO);
        try {
            return ResponseEntity.ok(dataSourceService.executeQuery2(reportTemplate));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
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
