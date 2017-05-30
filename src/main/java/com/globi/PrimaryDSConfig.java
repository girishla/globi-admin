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

@Configuration
public class PrimaryDSConfig {

	
	@Bean(name = "dsMDT")
	@ConfigurationProperties("app.datasource.mdt")
	@Primary
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}

	
    @Bean(name = "jdbcOracleMDT") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsMDT") DataSource dsMDT) { 
        return new JdbcTemplate(dsMDT); 
    } 
	
}
