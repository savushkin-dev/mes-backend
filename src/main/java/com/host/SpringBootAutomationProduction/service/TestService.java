package com.host.SpringBootAutomationProduction.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TestService {

//    private final JdbcTemplate secondJdbcTemplate;
//
//    @Autowired
//    public TestService(@Qualifier("secondJdbcTemplate")JdbcTemplate secondJdbcTemplate) {
//        this.secondJdbcTemplate = secondJdbcTemplate;
//    }
//
//    public List<Map<String, Object>> getDataFromSecondDb() {
//        String sql = "SELECT * FROM BD_LUMOVE"; // Замените на ваш SQL-запрос
//        return secondJdbcTemplate.queryForList(sql);
//    }

}
