package com.host.SpringBootAutomationProduction.util;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ReportUtil { //Вспомогательный класс для формирования данных для отчета

    Map<String, Object> result = new HashMap<>();

    public ReportUtil() {
        result.put("globalVar", new HashMap<String, Object>());
        result.put("tableData", null);
    }

    public void addData(List<Map<String, Object>> rows){
        result.put("tableData", rows);
    }

    public void addVar(Map<String, Object> rows){
        result.put("globalVar", rows);
    }



}
