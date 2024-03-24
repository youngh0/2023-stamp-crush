package com.stampcrush.backend.config;

import com.stampcrush.backend.application.manager.NamedLockService;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasourceConfig {

    @ConfigurationProperties("spring.datasource.hikari")
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public NamedLockService namedLockService() {
        return new NamedLockService(dataSource());
    }
}
