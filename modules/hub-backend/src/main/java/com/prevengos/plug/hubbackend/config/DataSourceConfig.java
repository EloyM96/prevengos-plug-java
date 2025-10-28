package com.prevengos.plug.hubbackend.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("hub.sqlserver")
    public DataSourceProperties sqlServerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource sqlServerDataSource(DataSourceProperties sqlServerDataSourceProperties) {
        return sqlServerDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource sqlServerDataSource) {
        return new NamedParameterJdbcTemplate(sqlServerDataSource);
    }
}
