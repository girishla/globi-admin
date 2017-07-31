package com.globi;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ConfigurationProperties("app.datasource.mdt")
public class PrimaryDSConfig  extends HikariConfig {

	
	@Bean(name = "dsMDT")
	@Primary
	public DataSource dataSource() {
		return new HikariDataSource(this);
	}

	
    @Bean(name = "jdbcOracleMDT") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsMDT") DataSource dsMDT) { 
        return new JdbcTemplate(dsMDT); 
    } 
	
}
