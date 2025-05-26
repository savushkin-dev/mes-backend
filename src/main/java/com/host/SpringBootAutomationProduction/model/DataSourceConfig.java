package com.host.SpringBootAutomationProduction.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceConfig {
    private String url;
    private String username;
    private String password;
    private String driverClassName; // например: "org.postgresql.Driver"


}
