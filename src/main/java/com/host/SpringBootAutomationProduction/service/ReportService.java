package com.host.SpringBootAutomationProduction.service;


import com.host.SpringBootAutomationProduction.exceptions.ReportTemplateNotFoundException;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import com.host.SpringBootAutomationProduction.repositories.postgres.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }


    public ReportTemplate findByReportName(String reportName) {
        Optional<ReportTemplate> reportTemplateOpt = reportRepository.findByReportName(reportName);
        return reportTemplateOpt.orElseThrow(() -> new ReportTemplateNotFoundException("ReportTemplate not found with report name: " + reportName));

    }

    public void saveOrUpdateReport(ReportTemplate reportTemplate) {
        Optional<ReportTemplate> reportTemplateOpt = reportRepository.findByReportName(reportTemplate.getReportName());

        if(reportTemplateOpt.isPresent()) {
            ReportTemplate reportTemplateToUpdate = reportTemplateOpt.get();
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



}
