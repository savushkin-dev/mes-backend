package com.host.SpringBootAutomationProduction.configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import javax.sql.DataSource;
import java.util.HashMap;

//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        basePackages = "com.host.SpringBootAutomationProduction.repositories.sqlserver",
//        entityManagerFactoryRef = "sqlServerEntityManagerFactory",
//        transactionManagerRef = "sqlServerTransactionManager"
//)
public class SqlServerConfig {

//    @Bean(name = "sqlServerDataSource")
//    @ConfigurationProperties(prefix = "spring.sqlserver.datasource")
//    public DataSource dataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "sqlServerEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
//            @Qualifier("sqlServerDataSource") DataSource dataSource, EntityManagerFactoryBuilder builder) {
////        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
////        em.setDataSource(dataSource);
////        em.setPackagesToScan("com.host.SpringBootAutomationProduction.model.sqlserver");
////        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
////        em.setJpaPropertyMap(new HashMap<>());
////        return em;
//
//        HashMap<String, Object> properties = new HashMap<>();
//        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
//
//        return builder.dataSource(dataSource)
//                .properties(properties)
//                .packages("com.host.SpringBootAutomationProduction.model.postgres")
//                .persistenceUnit("postgres")
//                .build();
//    }
//
//    @Bean(name = "sqlServerTransactionManager")
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("sqlServerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
}