package com.globi.infa.datasource.fbm;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
@Configuration
public class OracleFBMConfig {


	 @Bean(name = "dsFBM") 
	 @ConfigurationProperties("app.datasource.fbm")
	    public DataSource dataSourceFBM() { 
		  return DataSourceBuilder
			        .create()
			        .build();
	    } 

    
    @Bean(name = "jdbcOracleFBM") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsFBM") DataSource dsFBM) { 
        return new JdbcTemplate(dsFBM); 
    } 
    
}