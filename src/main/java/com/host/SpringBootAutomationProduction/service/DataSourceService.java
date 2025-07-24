package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.exceptions.DataSourceNotRequestedException;
import com.host.SpringBootAutomationProduction.model.DataSourceConfig;
import com.host.SpringBootAutomationProduction.model.postgres.ReportTemplate;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
        try {
            DataSource dataSource = createDataSource(config);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("Error executing query: {}, config: {}", sql, config);
            throw new DataSourceNotRequestedException(e.getMessage());
        }
    }

    public List<Map<String, Object>> executeQuery(String sql, DataSourceConfig config,
                                                  Map<String, String> parameters) {
        try {
            DataSource dataSource = createDataSource(config);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);


            String[] parts = sql.split("(?i)ORDER BY");
            String mainSql = parts[0].trim();
            String orderByClause = parts.length > 1 ? parts[1] : null;

            if (orderByClause != null) {
                orderByClause = replaceSortParameters(orderByClause, parameters);
            }

            String finalSql = orderByClause != null ? mainSql + " ORDER BY " + orderByClause : mainSql;

            String sqlPrepared = finalSql.replaceAll("(?<=\\s|^):(\\w+)(?=\\s|$)", "?");
            return jdbcTemplate.query(sqlPrepared, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {

                    Pattern pattern = Pattern.compile("(?<=\\s|^):(\\w+)(?=\\s|$)");
                    Matcher matcher = pattern.matcher(finalSql);
                    int index = 1;

                    while (matcher.find()) {
                        String paramName = matcher.group().substring(1);
                        String value = parameters.get(paramName);


                        if (value == null) {
                            ps.setNull(index, Types.NULL);
                        } else if (isDate(value)) {
                            ps.setDate(index, java.sql.Date.valueOf(value));
                        } else if (isNumeric(value)) {
                            ps.setBigDecimal(index, new BigDecimal(value));
                        } else if (isBoolean(value)) {
                            ps.setBoolean(index, Boolean.parseBoolean(value));
                        } else {
                            ps.setString(index, value);
                        }
                        index++;
                    }
                }
            }, new ColumnMapRowMapper());
        } catch (Exception e) {
            log.error("Error executing query: {}, parameters: {}, config: {}", sql, parameters, config);
            throw new DataSourceNotRequestedException(e.getMessage());
        }
    }

    private String replaceSortParameters(String orderByClause, Map<String, String> parameters) {

        Pattern pattern = Pattern.compile(":(\\w+)");
        Matcher matcher = pattern.matcher(orderByClause);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            String value = parameters.get(paramName);

            if (value != null) {
                String escapedValue = value.replace("'", "''");
                matcher.appendReplacement(result, escapedValue);
            } else {
                matcher.appendReplacement(result, matcher.group());
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }


    private boolean isDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNumeric(String value) {
        return value.matches("-?\\d+(\\.\\d+)?");
    }

    private boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }


    public Map<String, String> splitSqlByTableName(String inputSql) {
        Map<String, String> result = new LinkedHashMap<>();
        if (inputSql == null || inputSql.isBlank()) return result;

        List<String> queries = splitSqlString(inputSql);

//        for (String query : queries) {
//            if(!isValidSelectQuery(query)){
//                throw new RuntimeException("Ошибка запроса: " + query);
//            }
//        } // ругается на AS в запросе

        for (String query : queries) {
            String table = extractTableName(query);
            result.put(table, query);
        }

        return result;
    }

    public List<String> splitSqlString(String inputSql) {
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
        return queries;
    }

    public boolean isValidSelectQuery(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) return false;

            String upperSql = sql.toUpperCase();

            // Список слов которые запрещены
            String[] forbiddenKeywords = {
                    "INSERT", "UPDATE", "DELETE", "DROP", "ALTER",
                    "TRUNCATE", "CREATE", "GRANT", "REVOKE",
                    "EXEC", "EXECUTE", "MERGE", "CALL", "USE"
            };

            for (String keyword : forbiddenKeywords) {
                if (upperSql.contains(keyword)) {
                    return false;
                }
            }

            if (!upperSql.trim().startsWith("SELECT")) {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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


}
