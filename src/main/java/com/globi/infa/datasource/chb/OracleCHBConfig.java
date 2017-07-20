package com.globi.infa.datasource.chb;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration
public class OracleCHBConfig {


	 @Bean(name = "dsCHB") 
	 @ConfigurationProperties("app.datasource.chb")
	    public DataSource dataSourceCHB() { 
		  return DataSourceBuilder
			        .create()
			        .build();
	    } 

    
    @Bean(name = "jdbcOracleCHB") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsCHB") DataSource dsCHB) { 
        return new JdbcTemplate(dsCHB); 
    } 
    
}