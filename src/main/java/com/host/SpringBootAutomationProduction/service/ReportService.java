package com.host.SpringBootAutomationProduction.service;


import com.host.SpringBootAutomationProduction.exceptions.ReportTemplateNotFoundException;
import com.host.SpringBootAutomationProduction.model.DataSourceConfig;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    private final DataSourceService dataSourceService;



    @Autowired
    public ReportService(ReportRepository reportRepository, DataSourceService dataSourceService) {
        this.reportRepository = reportRepository;
        this.dataSourceService = dataSourceService;
    }


    public ReportTemplate findByReportName(String reportName) {
        Optional<ReportTemplate> reportTemplateOpt = reportRepository.findByReportName(reportName);
        return reportTemplateOpt.orElseThrow(() ->
                new ReportTemplateNotFoundException("ReportTemplate not found with report name: " + reportName));

    }

    @Transactional
    public void saveOrUpdateReport(ReportTemplate reportTemplate) {
        Optional<ReportTemplate> reportTemplateOpt = reportRepository.findByReportName(reportTemplate.getReportName());

        if(reportTemplateOpt.isPresent()) {
            ReportTemplate reportTemplateToUpdate = reportTemplateOpt.get();
            reportTemplateToUpdate.setReportCategory(reportTemplate.getReportCategory());
            reportTemplateToUpdate.setDbUrl(reportTemplate.getDbUrl());
            reportTemplateToUpdate.setDbUsername(reportTemplate.getDbUsername());
            reportTemplateToUpdate.setDbPassword(reportTemplate.getDbPassword());
            reportTemplateToUpdate.setDbDriver(reportTemplate.getDbDriver());
            reportTemplateToUpdate.setSql(reportTemplate.getSql());
            reportTemplateToUpdate.setContent(reportTemplate.getContent());
            reportTemplateToUpdate.setStyles(reportTemplate.getStyles());
            reportTemplateToUpdate.setParameters(reportTemplate.getParameters());
            reportRepository.save(reportTemplateToUpdate);
            log.info("Report updated successfully with report name: {}", reportTemplate.getReportName());
        } else {
            reportRepository.save(reportTemplate);
            log.info("Report created successfully with report name: {}", reportTemplate.getReportName());
        }
    }

    public String getParameters(String reportName) {
        ReportTemplate reportTemplate = findByReportName(reportName);
        return reportTemplate.getParameters();
    }



    public List<String> findAllReportName() {
        List<String> reportsName = new ArrayList<>();

        List<ReportTemplate> reportTemplates = reportRepository.findAll();
        for(ReportTemplate reportTemplate : reportTemplates) {
            reportsName.add(reportTemplate.getReportName());
        }
        return reportsName;
    }

    public Map<?,?> getDataByReportName(String reportName, Map<String, String> parameters) {
        ReportTemplate reportTemplate = findByReportName(reportName);
        return getDataForReport(reportTemplate, parameters);
    }


    public Map<?,?> getDataForReport(ReportTemplate reportTemplate, Map<String, String> parameters) {
        try {

            DataSourceConfig config = new DataSourceConfig(reportTemplate.getDbUrl(), reportTemplate.getDbUsername(),
                    reportTemplate.getDbPassword(), reportTemplate.getDbDriver());

            List<Map<String, Object>> tableData = new ArrayList<>();

            Map<String, String> sqlQueries = dataSourceService.splitSqlByTableName(reportTemplate.getSql());

            for (Map.Entry<String, String> entry : sqlQueries.entrySet()) {
                String tableName = entry.getKey();
                String sql = entry.getValue();

                List<Map<String, Object>> rows = dataSourceService.executeQuery(sql, config, parameters);

                Map<String, Object> tableBlock = new HashMap<>();
                tableBlock.put("tableName", tableName);
                tableBlock.put("data", rows);

                tableData.add(tableBlock);
            }


            // Преобразуем globalVars из Map<String, String> в List<Map<String, String>> как в dataReportTest
            List<Map<String, String>> globalVarList = new ArrayList<>();

            Map<String, Object> reportResult = new HashMap<>();
            reportResult.put("globalVar", globalVarList);
            reportResult.put("tableData", tableData);

            return reportResult;
        } catch (Exception e){
            log.error("Error generating data for report: {}", reportTemplate);
            throw e;
        }

    }

    public List<Map<String, Object>> getReportsNameGroupedByCategory() {
        List<ReportTemplate> allReports = reportRepository.findAll();

        Map<String, List<String>> tempMap = new LinkedHashMap<>();

        for (ReportTemplate report : allReports) {
            tempMap.computeIfAbsent(report.getReportCategory(), k -> new ArrayList<>())
                    .add(report.getReportName());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : tempMap.entrySet()) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("category", entry.getKey());
            categoryMap.put("reports", entry.getValue());
            result.add(categoryMap);
        }

        return result;
    }



}
