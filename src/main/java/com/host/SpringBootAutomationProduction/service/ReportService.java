package com.host.SpringBootAutomationProduction.service;


import com.host.SpringBootAutomationProduction.exceptions.ReportTemplateNotFoundException;
import com.host.SpringBootAutomationProduction.model.DataSourceConfig;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportRepository;
import com.host.SpringBootAutomationProduction.util.ReportUtil;
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

    private final ExecutorScriptService executorScriptService;
    

    @Autowired
    public ReportService(ReportRepository reportRepository, DataSourceService dataSourceService, ExecutorScriptService executorScriptService) {
        this.reportRepository = reportRepository;
        this.dataSourceService = dataSourceService;
        this.executorScriptService = executorScriptService;
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
            reportTemplateToUpdate.setScript(reportTemplate.getScript());
            reportTemplateToUpdate.setSqlMode(reportTemplate.isSqlMode());
            reportTemplateToUpdate.setDataBands(reportTemplate.getDataBands());
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

            if(!reportTemplate.isSqlMode()){
                return getDataByScript(reportTemplate, parameters);
            }

            ReportUtil reportUtil = new ReportUtil();

            DataSourceConfig config = new DataSourceConfig(reportTemplate.getDbUrl(), reportTemplate.getDbUsername(),
                    reportTemplate.getDbPassword(), reportTemplate.getDbDriver());

            Map<String, String> sqlQueries = dataSourceService.splitSqlByTableName(reportTemplate.getSql());

            for (Map.Entry<String, String> entry : sqlQueries.entrySet()) {
                String tableName = entry.getKey();
                String sql = entry.getValue();
                List<Map<String, Object>> rows = dataSourceService.executeQuery(sql, config, parameters);
                reportUtil.addData(rows, tableName);
            }

            return reportUtil.getResult();
        } catch (Exception e){
            log.error("Error generating data for report: {}", reportTemplate);
            throw e;
        }

    }

    public Map<?,?> getDataByScript(ReportTemplate reportTemplate, Map<String, String> parameters) {
        Map<?,?> result = (Map<?, ?>) executorScriptService.executeScript(reportTemplate.getScript(), parameters);
        return result;
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
