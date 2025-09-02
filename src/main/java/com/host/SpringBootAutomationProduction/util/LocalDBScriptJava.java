package com.host.SpringBootAutomationProduction.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import com.host.SpringBootAutomationProduction.util.ReportUtil; //Вспомогательный класс

import javax.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class LocalDBScriptJava {

    public static Map<?,?> main2(Map<String, String> params) {
        ReportUtil reportUtil = new ReportUtil();

        // Настройка DataSource
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://10.230.0.10:5432/asutp");
        dataSource.setUsername("reportsuser");
        dataSource.setPassword("reports");
        dataSource.setDriverClassName("org.postgresql.Driver");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String startTime = params.get("startTime");
        String endTime = params.get("endTime");

        // Построение основного SQL (исправлено для безопасности)
        StringBuilder sqlWhereBuilder = new StringBuilder();
        List<Object> sqlParams = new ArrayList<>();

        sqlWhereBuilder.append("FROM reports.cip ")
                .append("LEFT JOIN reports.cip_programs ON reports.cip_programs.program_id = reports.cip.program ")
                .append("AND reports.cip.cipno = reports.cip_programs.cip_no AND reports.cip_programs.site_id = 1 ")
                .append("WHERE endtime::date >= ? AND endtime::date <= ?");

        sqlParams.add(startTime);
        sqlParams.add(endTime);

        // Фильтры (добавляем условия и параметры)
        if(!Boolean.parseBoolean(params.get("allMca"))){
            List<Integer> trueNumbers = params.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("mcap"))
                    .filter(entry -> Boolean.parseBoolean(entry.getValue()))
                    .map(entry -> Integer.parseInt(entry.getKey().substring(4)))
                    .collect(Collectors.toList());

            if(!trueNumbers.isEmpty()){
                String placeholders = trueNumbers.stream()
                        .map(n -> "?")
                        .collect(Collectors.joining(","));
                sqlWhereBuilder.append(" AND cipno IN (").append(placeholders).append(")");
                sqlParams.addAll(trueNumbers);
            }
        }

        if(!Boolean.parseBoolean(params.get("allLine"))){
            sqlWhereBuilder.append(" AND lineno = ?");
            sqlParams.add(Integer.parseInt(params.get("line")));
        }

        if(!params.get("objWash").isEmpty()){
            sqlWhereBuilder.append(" AND objectname SIMILAR TO ?");
            sqlParams.add(params.get("objWash") + "%");
        }

        String sqlWhere = sqlWhereBuilder.toString();
        Object[] paramsArray = sqlParams.toArray();

        // Сортировка
        StringBuilder orderBuilder = new StringBuilder();
        if(Boolean.parseBoolean(params.get("orderStart"))) orderBuilder.append("starttime,");
        if(Boolean.parseBoolean(params.get("orderCipno"))) orderBuilder.append("cipno,");
        if(Boolean.parseBoolean(params.get("orderSo"))) orderBuilder.append("so,");
        if(Boolean.parseBoolean(params.get("orderErr"))) orderBuilder.append("err,");
        if(Boolean.parseBoolean(params.get("orderObj"))) orderBuilder.append("objectname,");

        String orderStr = orderBuilder.length() > 0 ?
                orderBuilder.substring(0, orderBuilder.length()-1) : "starttime";

        // Основной SQL
        String sqlSelect = "SELECT site_id,cipno,lineno,objectname,program_name," +
                "TO_CHAR(starttime, 'DD.MM.YYYY HH24:MI:SS') AS starttime, " +
                "TO_CHAR(endtime, 'DD.MM.YYYY HH24:MI:SS') AS endtime," +
                "TO_CHAR((endtime - starttime), 'HH24:MI:SS') AS dur," +
                "watersecond,waterclean,qavk,qavs,so,ro,err," +
                "p_czad_k,p_t_k,ptm_k,p_czad_s,p_t_s,ptm_s,p_t_d,ptm_d,opers,errors,rate ";

        String finalSql = sqlSelect + sqlWhere + " ORDER BY " + orderStr;

        // ВЫПОЛНЕНИЕ В МНОГОПОТОЧНОМ РЕЖИМЕ
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CompletionService<List<Map<String, Object>>> completionService =
                new ExecutorCompletionService<>(executor);

        long startTimeTotal = System.currentTimeMillis();

        // Запускаем все запросы параллельно
        Future<List<Map<String, Object>>> mainQueryFuture = completionService.submit(() -> {
            return jdbcTemplate.queryForList(finalSql, paramsArray);
        });

        Future<List<Map<String, Object>>> acidQueryFuture = completionService.submit(() -> {
            String acidSql = "SELECT SUM(rate) as res " + sqlWhere + " and program = 256";
            return jdbcTemplate.queryForList(acidSql, paramsArray);
        });

        Future<List<Map<String, Object>>> alkaliQueryFuture = completionService.submit(() -> {
            String alkaliSql = "SELECT SUM(rate) as res " + sqlWhere + " and program = 512";
            return jdbcTemplate.queryForList(alkaliSql, paramsArray);
        });

        // Ожидаем результаты
        try {
            // Основной запрос
            List<Map<String, Object>> rows = mainQueryFuture.get();

            double sumWaterSecond = 0;
            double sumWaterClean = 0;
            for (Map<String, Object> row : rows) {
                Map<String, String> styles = new HashMap<>();
                sumWaterSecond += Double.parseDouble(row.get("watersecond").toString());
                sumWaterClean += Double.parseDouble(row.get("waterclean").toString());
                row.put("style", styles);
            }

            reportUtil.addData(rows, "mca");

            Map<String,Object> vars = new HashMap<>();
            vars.put("start", startTime);
            vars.put("end", endTime);
            vars.put("sumwatersecond", sumWaterSecond);
            vars.put("sumwaterclean", sumWaterClean);

            // Получаем результаты дополнительных запросов
            List<Map<String, Object>> acidResult = acidQueryFuture.get();
            List<Map<String, Object>> alkaliResult = alkaliQueryFuture.get();

            vars.put("resK", acidResult.isEmpty() ? 0 : acidResult.get(0).get("res"));
            vars.put("resCh", alkaliResult.isEmpty() ? 0 : alkaliResult.get(0).get("res"));

            reportUtil.addVar(vars);

            long endTimeTotal = System.currentTimeMillis();
            System.out.println("Общее время выполнения: " + (endTimeTotal - startTimeTotal) + " ms");

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Ошибка при выполнении запросов", e);
        } finally {
            executor.shutdown();
        }

        return reportUtil.getResult();
    }

    public static Map<?,?> main(Map<String, String> params) {

        ReportUtil reportUtil = new ReportUtil(); //Используем специальный вспомогательный имортированный класс

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://10.230.0.10:5432/asutp");
        dataSource.setUsername("reportsuser");
        dataSource.setPassword("reports");
        dataSource.setDriverClassName("org.postgresql.Driver");



        String startTime = params.get("startTime");
        String endTime = params.get("endTime");

        String sqlSelect = "SELECT\n" +
                "    site_id,cipno,lineno,objectname,program_name,TO_CHAR(starttime, 'DD.MM.YYYY HH24:MI:SS') AS starttime, TO_CHAR(endtime, 'DD.MM.YYYY HH24:MI:SS') AS endtime,TO_CHAR((endtime - starttime), 'HH24:MI:SS') AS dur,\n" +
                "    watersecond,waterclean,qavk,qavs,so,ro,err,p_czad_k,p_t_k,ptm_k,p_czad_s,p_t_s,ptm_s,p_t_d,ptm_d,opers,errors,rate\n " ;

        String sql =
                "FROM reports.cip\n" +
                        "        LEFT JOIN reports.cip_programs ON reports.cip_programs.program_id = reports.cip.program AND reports.cip.cipno = reports.cip_programs.cip_no  AND reports.cip_programs.site_id = 1 \n" +
                        "WHERE endtime::date >= '"+ startTime +"' AND endtime::date <= '"+ endTime +"'";

        if(!Boolean.parseBoolean(params.get("allLine"))){
            sql += " AND lineno = " + params.get("line") + " ";
        }

        if(!params.get("objWash").isEmpty()){
            sql += " AND objectname SIMILAR TO ('"+ params.get("objWash") +"%') ";
        }



        if(!Boolean.parseBoolean(params.get("allMca"))){

            List<Integer> trueNumbers = params.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("mcap"))
                    .filter(entry -> Boolean.parseBoolean(entry.getValue()))
                    .map(entry -> Integer.parseInt(entry.getKey().substring(4)))
                    .toList();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < trueNumbers.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(trueNumbers.get(i));
            }

            sql += " AND cipno IN ("+ sb.toString() +") ";
        } else {
            sql += " AND cipno <> 1 ";
        }

        String sqlWhere = sql;


        String orderStr = "";
        if(Boolean.parseBoolean(params.get("orderStart"))){
            orderStr += "starttime,";
        }
        if(Boolean.parseBoolean(params.get("orderCipno"))){
            orderStr += "cipno,";
        }
        if(Boolean.parseBoolean(params.get("orderSo"))){
            orderStr += "so,";
        }
        if(Boolean.parseBoolean(params.get("orderErr"))){
            orderStr += "err,";
        }
        if(Boolean.parseBoolean(params.get("orderObj"))){
            orderStr += "objectname,";
        }

        if(!orderStr.isEmpty()){
            orderStr = orderStr.substring(0, orderStr.length()-1);
            sql += " ORDER BY "+orderStr;
        } else {
            sql += " ORDER BY starttime ";
        }






        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlSelect + sql);
        System.out.println("Основной sql: " + sqlSelect + sql);

        double sumWaterSecond = 0;
        double sumWaterClean = 0;
        for (Map<String, Object> row : rows) { //Добавляем стили при желании
            Map<String, String> styles = new HashMap<>();

            sumWaterSecond += Double.parseDouble(row.get("watersecond").toString());
            sumWaterClean += Double.parseDouble(row.get("waterclean").toString());

            row.put("style", styles);

        }


        reportUtil.addData(rows, "mca"); //Просто используем методы вспомогательного класса для формирования результата

        Map<String,Object> vars = new HashMap<String,Object>();
        vars.put("start", params.get("startTime"));
        vars.put("end", params.get("endTime"));
        vars.put("sumwatersecond", sumWaterSecond);
        vars.put("sumwaterclean", sumWaterClean);

        String sqlK = "SELECT SUM(rate) as res " + sqlWhere + " and program = 256";
        String sqlCh = "SELECT SUM(rate) as res " + sqlWhere + " and program = 512";

        System.out.println("Расход кислоты sql: " + sqlK);
        rows = jdbcTemplate.queryForList(sqlK);
        vars.put("resK", rows.get(0).get("res"));

        System.out.println("Расход щелочи sql: " + sqlCh);
        rows = jdbcTemplate.queryForList(sqlCh);
        vars.put("resCh", rows.get(0).get("res"));



        reportUtil.addVar(vars);


        return reportUtil.getResult(); //Используем метод вспомогательного класса для получения сформированного ответа
    }
}
