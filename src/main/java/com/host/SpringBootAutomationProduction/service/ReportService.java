package com.host.SpringBootAutomationProduction.service;


import com.host.SpringBootAutomationProduction.exceptions.ReportTemplateNotFoundException;
import com.host.SpringBootAutomationProduction.model.DataSourceConfig;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
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
        return reportTemplateOpt.orElseThrow(() -> new ReportTemplateNotFoundException("ReportTemplate not found with report name: " + reportName));

    }

    public void saveOrUpdateReport(ReportTemplate reportTemplate) {
        Optional<ReportTemplate> reportTemplateOpt = reportRepository.findByReportName(reportTemplate.getReportName());

        if(reportTemplateOpt.isPresent()) {
            ReportTemplate reportTemplateToUpdate = reportTemplateOpt.get();
            reportTemplateToUpdate.setDbUrl(reportTemplate.getDbUrl());
            reportTemplateToUpdate.setDbUsername(reportTemplate.getDbUsername());
            reportTemplateToUpdate.setDbPassword(reportTemplate.getDbPassword());
            reportTemplateToUpdate.setDbDriver(reportTemplate.getDbDriver());
            reportTemplateToUpdate.setSql(reportTemplate.getSql());
            reportTemplateToUpdate.setContent(reportTemplate.getContent());
            reportTemplateToUpdate.setStyles(reportTemplate.getStyles());
            reportRepository.save(reportTemplateToUpdate);
        } else {
            reportRepository.save(reportTemplate);
        }
    }

    public List<String> findAllReportName() {
        List<String> reportsName = new ArrayList<>();

        List<ReportTemplate> reportTemplates = reportRepository.findAll();
        for(ReportTemplate reportTemplate : reportTemplates) {
            reportsName.add(reportTemplate.getReportName());
        }
        return reportsName;
    }

    public Map<?,?> getDataForReport(String reportName) {
        try {
            ReportTemplate reportTemplate = findByReportName(reportName);
            DataSourceConfig config = new DataSourceConfig(reportTemplate.getDbUrl(), reportTemplate.getDbUsername(),
                    reportTemplate.getDbPassword(), reportTemplate.getDbDriver());

            List<Map<String, Object>> tableData = new ArrayList<>();

            Map<String, String> sqlQueries = dataSourceService.splitSqlByTableName(reportTemplate.getSql());

            for (Map.Entry<String, String> entry : sqlQueries.entrySet()) {
                String tableName = entry.getKey();
                String sql = entry.getValue();

                List<Map<String, Object>> rows = dataSourceService.executeQuery(sql, config);

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
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }



}
