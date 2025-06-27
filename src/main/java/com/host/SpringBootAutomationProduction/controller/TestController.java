package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.service.DataSourceService;
import com.host.SpringBootAutomationProduction.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
public class TestController {

    private final TestService testService;


    private final DataSourceService dataSourceService;

    @Autowired
    public TestController(TestService testService, DataSourceService dataSourceService) {
        this.testService = testService;
        this.dataSourceService = dataSourceService;
    }


    @GetMapping("/1")
    public ResponseEntity<?> test1() {
        return ResponseEntity.ok("");
    }


}
