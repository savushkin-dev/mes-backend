package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.service.DataSourceService;
import com.host.SpringBootAutomationProduction.service.ExecutorScriptService;
import com.host.SpringBootAutomationProduction.service.TestService;
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

    @GetMapping("/comp1")
    public ResponseEntity<?> test() {

        Map<String, String> params = new HashMap<>();
        params.put("name", "John");
        params.put("age", "30");
        // 1. Простейший пример (без параметров)
        try {
            String helloWorldCode =
                    "import java.util.*; \n" +
                    "public class DynamicScript {\n" +
                            "  public static String execute(Map<String, String> params) {\n" +
                            "    return \"Hello, \" + params.get(\"name\") + \"! Age: \" + params.get(\"age\");\n" +
                            "  }\n" +
                            "}";

            Object result = executorScriptService.executeScript(helloWorldCode, params);
            System.out.println(result); // Выведет: Hello from dynamic script!
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
        }



        return ResponseEntity.ok("");
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
