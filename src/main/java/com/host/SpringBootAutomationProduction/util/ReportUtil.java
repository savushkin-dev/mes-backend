package com.host.SpringBootAutomationProduction.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ReportUtil { //Вспомогательный класс для формирования данных для отчета

    Map<String, Object> result = new HashMap<>();


    public void addData(List<Map<String, Object>> rows, String bandName){
        List<Map<String, Object>> tableData = new ArrayList<>();
        Map<String, Object> tableBlock = new HashMap<>();
        tableBlock.put("tableName", bandName);
        tableBlock.put("data", rows);
        tableData.add(tableBlock);
        result.put("tableData", tableData);
    }

    public void addVar(List<Map<String, Object>> rows, String bandName){
        List<Map<String, String>> globalVarList = new ArrayList<>();
        result.put("globalVar", globalVarList);
    }


}
