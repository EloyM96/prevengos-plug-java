package com.prevengos.plug.hubbackend.config;

import com.prevengos.plug.gateway.csv.CsvFileWriter;
import com.prevengos.plug.gateway.filetransfer.FileTransferClient;
import com.prevengos.plug.gateway.filetransfer.DefaultFileTransferClient;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(SqlServerProperties properties) {
        return DataSourceBuilder.create()
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public FileTransferClient fileTransferClient() {
        return new DefaultFileTransferClient();
    }

    @Bean
    public CsvFileWriter csvFileWriter() {
        return new CsvFileWriter();
    }
}
