package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.service.LuMoveService;
import com.host.SpringBootAutomationProduction.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/test")
public class TestController {

    private final TestService testService;
    private final LuMoveService luMoveService;

    @Autowired
    public TestController(TestService testService, LuMoveService luMoveService) {
        this.testService = testService;
        this.luMoveService = luMoveService;
    }


    @GetMapping("/1")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(luMoveService.getLuMoveDay());

//        return ResponseEntity.ok("test!");
    }


}
