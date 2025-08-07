package com.host.SpringBootAutomationProduction.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalDBScriptJava {

    public static Map<?,?> main(Map<String, String> params) {

        params.put("datefinish", "2025-08-04");

        ReportUtil reportUtil = new ReportUtil(); //Используем специальный вспомогательный имортированный класс

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://10.230.0.10:5432/asutp");
        dataSource.setUsername("reportsuser");
        dataSource.setPassword("reports");
        dataSource.setDriverClassName("org.postgresql.Driver");


        String sql = "SELECT postnumber, carnumber, section, TO_CHAR(starttime, 'DD.MM.YYYY HH24:MI:SS') AS starttime, " +
                "TO_CHAR(finishtime, 'DD.MM.YYYY HH24:MI:SS') AS finishtime, total, stops, nprobe, teafter, tebefore, waitingval FROM posts " +
                "WHERE finishtime::date = '" + params.get("datefinish") + "';";


        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        for (Map<String, Object> row : rows) {
            Map<String, String> styles = new HashMap<>();

            if (row.get("postnumber") != null) {
                try {
//                    int numberValue = Integer.parseInt(row.get("postnumber").toString());
                    double numberValue = Double.parseDouble(row.get("postnumber").toString());
                    if (numberValue > 100) {
                        styles.put("postnumber", "color: red;");
                    } else {
                        styles.put("postnumber", "color: blue;");
                    }
                } catch (NumberFormatException e) {
                    // Если не число, оставляем без стиля
                    throw e;
                }
            }

//            if (!styles.isEmpty()) {
                row.put("style", styles);
//            }

        }


        reportUtil.addData(rows, "posts"); //Просто используем методы вспомогательного класса для формирования результата

        return reportUtil.getResult(); //Используем метод вспомогательного класса для получения сформированного ответа
    }
}
