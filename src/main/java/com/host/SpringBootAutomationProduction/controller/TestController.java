package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.service.DataSourceService;
import com.host.SpringBootAutomationProduction.service.ExecutorScriptService;
import com.host.SpringBootAutomationProduction.service.TestService;
import com.host.SpringBootAutomationProduction.util.LocalDBScriptJava;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/1")
    public ResponseEntity<?> test1() {
        return ResponseEntity.ok("");
    }

    @GetMapping("/2")
    public ResponseEntity<?> test() {


        return ResponseEntity.ok(LocalDBScriptJava.main(new HashMap<>()));
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
