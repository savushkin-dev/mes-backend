package com.host.SpringBootAutomationProduction.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataSourceConfig {
    private String url;
    private String username;
    private String password;
    private String driverClassName; // например: "org.postgresql.Driver"


}
