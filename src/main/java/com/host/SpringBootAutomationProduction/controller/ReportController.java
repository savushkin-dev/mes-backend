package com.host.SpringBootAutomationProduction.controller;


import com.host.SpringBootAutomationProduction.dto.ParametersDTO;
import com.host.SpringBootAutomationProduction.dto.ReportGlobalVarsDTO;
import com.host.SpringBootAutomationProduction.dto.ReportTemplateParametersDTO;
import com.host.SpringBootAutomationProduction.dto.ReportTemplateDTO;
import com.host.SpringBootAutomationProduction.model.postgres.ReportGlobalVars;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.service.ReportGlobalVarsService;
import com.host.SpringBootAutomationProduction.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;
    private final ReportGlobalVarsService globalVarsService;


    @Autowired
    public ReportController(ReportService reportService, ReportGlobalVarsService globalVarsService) {
        this.reportService = reportService;
        this.globalVarsService = globalVarsService;
    }

    @GetMapping("/{category}/{reportName}")
    public ReportTemplateDTO getReportByCategoryAndName(@PathVariable("category") String category,
                                                        @PathVariable("reportName") String reportName) {
        return convertToReportTemplateDTO(reportService.findByReportCategoryAndName(category, reportName)).encrypt();
    }

    @GetMapping("/{category}/{reportName}/parameters")
    public ResponseEntity<?> getTemplateParameters(
            @PathVariable("category") String category,
            @PathVariable("reportName") String reportName) {
        return ResponseEntity.ok(reportService.getParametersMeta(category, reportName));
    }

    @GetMapping("/names")
    public ResponseEntity<?> getReportNameList() {
        return ResponseEntity.ok(reportService.findAllReportName());
    }

    @GetMapping("/grouped-by-category")
    public ResponseEntity<List<Map<String, Object>>> getGroupedReports() {
        return ResponseEntity.ok(reportService.getReportsNameGroupedByCategory());
    }

    @GetMapping("/categories")
    public ResponseEntity<Set<String>> getCategories() {
        return ResponseEntity.ok(reportService.getAllReportCategories());
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
    @PostMapping("/create")
    public ResponseEntity<?> createOrUpdateReport(@RequestBody ReportTemplateDTO reportTemplateDTO) {
        ReportTemplate reportTemplate = convertToReportTemplate(reportTemplateDTO.decrypt());
        reportService.saveOrUpdateReport(reportTemplate);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/data/{category}/{reportName}")
    public ResponseEntity<?> getDataByReportName(
            @PathVariable("category") String category,
            @PathVariable("reportName") String reportName,
            @RequestBody ParametersDTO parameters) {
        return ResponseEntity.ok(
                reportService.getDataByReportName(category, reportName, parameters.getParameters())
        );
    }

    @PostMapping("/data")
    public ResponseEntity<?> getDataForReport(@RequestBody ReportTemplateParametersDTO templateParametersDTO) {
        ReportTemplate reportTemplate = convertToReportTemplate(templateParametersDTO.getReportTemplateDTO().decrypt());
        return ResponseEntity.ok(reportService.getDataForReport(reportTemplate, templateParametersDTO.getParameters()));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateReport(@RequestBody ReportTemplateDTO templateDTO) {
        ReportTemplate reportTemplate = convertToReportTemplate(templateDTO);
        reportService.updateReportNameAndCategoryById(reportTemplate);
        return ResponseEntity.ok("Report updated successfully");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable int id) {
        reportService.deleteReportById(id);
        return ResponseEntity.ok("Report deleted successfully");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
    @GetMapping("/globalVars")
    public ResponseEntity<List<ReportGlobalVarsDTO>> getGlobalVars() {
        List<ReportGlobalVarsDTO> globalVarsDTO = globalVarsService.getAllVars().stream()
                .map(this::convertToReportGlobalVarsDTO)
                .map(ReportGlobalVarsDTO::encrypt)  // Шифруем перед отправкой
                .collect(Collectors.toList());
        return ResponseEntity.ok(globalVarsDTO);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
    @PostMapping("/globalVars")
    public ResponseEntity<?> saveGlobalVars(@RequestBody List<ReportGlobalVarsDTO> reportGlobalVarsDTO) {
        List<ReportGlobalVars> reportGlobalVars = reportGlobalVarsDTO.stream()
                .map(ReportGlobalVarsDTO::decrypt)
                .map(this::convertToReportGlobalVars)
                .collect(Collectors.toList());
        globalVarsService.saveAllVars(reportGlobalVars);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    private ReportTemplate convertToReportTemplate(ReportTemplateDTO reportTemplateDTO) {
        return new ModelMapper().map(reportTemplateDTO, ReportTemplate.class);
    }

    private ReportTemplateDTO convertToReportTemplateDTO(ReportTemplate reportTemplate) {
        return new ModelMapper().map(reportTemplate, ReportTemplateDTO.class);
    }

    private ReportGlobalVars convertToReportGlobalVars(ReportGlobalVarsDTO reportGlobalVarsDTO) {
        return new ModelMapper().map(reportGlobalVarsDTO, ReportGlobalVars.class);
    }

    private ReportGlobalVarsDTO convertToReportGlobalVarsDTO(ReportGlobalVars reportGlobalVars) {
        return new ModelMapper().map(reportGlobalVars, ReportGlobalVarsDTO.class);
    }



}
