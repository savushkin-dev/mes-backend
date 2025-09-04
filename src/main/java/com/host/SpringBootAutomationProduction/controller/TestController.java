package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.service.DataSourceService;
import com.host.SpringBootAutomationProduction.service.ExecutorScriptService;
import com.host.SpringBootAutomationProduction.service.TestService;
import com.host.SpringBootAutomationProduction.util.LocalDBScriptJava;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/test")
public class TestController {

    private final TestService testService;


    private final DataSourceService dataSourceService;

    private final ExecutorScriptService executorScriptService;

    @Autowired
    public TestController(TestService testService, DataSourceService dataSourceService, ExecutorScriptService executorScriptService) {
        this.testService = testService;
        this.dataSourceService = dataSourceService;
        this.executorScriptService = executorScriptService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/1")
    public ResponseEntity<?> test1() {
        return ResponseEntity.ok("");
    }

    @GetMapping("/2")
    public ResponseEntity<?> test() {
        HashMap<String, String> map = new HashMap<>();
        map.put("allLine", "true");
        map.put("allMca", "true");
        map.put("endTime", "2025-08-21");
        map.put("line", "2");
        map.put("mca-child", "false");
        map.put("mcap1", "");
        map.put("mcap2", "");
        map.put("mcap3", "");
        map.put("mcap4", "");
        map.put("mcap5", "");
        map.put("mcap6", "");
        map.put("mcap7", "");
        map.put("mcap8", "");
        map.put("mcap9", "");
        map.put("mcap10", "");
        map.put("mcap11", "");
        map.put("mcap12", "");
        map.put("mcap13", "");
        map.put("mcap14", "");
        map.put("mcap15", "");
        map.put("mcap16", "");
        map.put("mcap17", "");
        map.put("mcap18", "");
        map.put("mcap19", "");
        map.put("mcap20", "");
        map.put("mcap28", "");
        map.put("objWash", "");
        map.put("orderCipno", "");
        map.put("orderErr", "");
        map.put("orderObj", "");
        map.put("orderSo", "");
        map.put("orderStartTime", "");
        map.put("startTime", "2025-08-21");


        return ResponseEntity.ok(LocalDBScriptJava.main(map));
    }

    @GetMapping("/comp2")
    public ResponseEntity<?> test2() {

        // 2. Пример с параметрами
        try {
            String calculatorCode = """
                
                
                public class HelloWorld {
                    public static double calculate(int a, double b, String operation) {
                        return switch (operation) {
                            case "+" -> a + b;
                            case "-" -> a - b;
                            case "*" -> a * b;
                            case "/" -> a / b;
                            default -> throw new IllegalArgumentException("Unknown operation");
                        };
                    }
                }
                """;

            Object result = null;

            System.out.println("Calculation result: " + result); // Выведет: 25.0
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
        }



        return ResponseEntity.ok("");
    }


}
