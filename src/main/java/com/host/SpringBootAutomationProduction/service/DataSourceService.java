package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.model.DataSourceConfig;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataSourceService {

    public DataSource createDataSource(DataSourceConfig config) {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(config.getUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setDriverClassName(config.getDriverClassName());
        return dataSource;
    }

    public List<Map<String, Object>> executeQuery(String sql, DataSourceConfig config) {
        DataSource dataSource = createDataSource(config);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, String> splitSqlByTableName(String inputSql) {
        Map<String, String> result = new LinkedHashMap<>();
        if (inputSql == null || inputSql.isBlank()) return result;

        List<String> queries = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < inputSql.length(); i++) {
            char c = inputSql.charAt(i);

            if (c == '\'') {
                inString = !inString;
                current.append(c);
            } else if (c == ';' && !inString) {
                String sql = current.toString().trim();
                if (!sql.isEmpty()) {
                    queries.add(sql);
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            queries.add(remaining);
        }

        for (String query : queries) {
            String table = extractTableName(query);
            result.put(table, query);
        }

        return result;
    }

    private String extractTableName(String sql) {
        String lower = sql.toLowerCase();

        Pattern pattern = Pattern.compile(
                "\\bfrom\\s+([a-zA-Z0-9_\\.]+)|\\binto\\s+([a-zA-Z0-9_\\.]+)|\\bupdate\\s+([a-zA-Z0-9_\\.]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(lower);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    return matcher.group(i);
                }
            }
        }

        return "unknown_table_" + UUID.randomUUID();
    }

    public List<Map<String, Object>> executeQuery2(ReportTemplate reportTemplate) throws SQLException {

        try (Connection conn = DriverManager.getConnection(reportTemplate.getDbUrl(),
                reportTemplate.getReportName(),
                reportTemplate.getDbPassword());
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(reportTemplate.getSql())) {

            List<Map<String, Object>> result = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                result.add(row);
            }

            return result;
        }


    }



}
