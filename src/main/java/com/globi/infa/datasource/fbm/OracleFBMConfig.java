package com.globi.infa.datasource.fbm;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
@Configuration
@ConfigurationProperties("app.datasource.fbm")
public class OracleFBMConfig  extends HikariConfig  {


	 @Bean(name = "dsFBM") 
	    public DataSource dataSourceFBM() { 
		 return new HikariDataSource(this);
	    } 

    
    @Bean(name = "jdbcOracleFBM") 
    @Autowired
    public JdbcTemplate jdbcTemplate(@Qualifier("dsFBM") DataSource dsFBM) { 
        return new JdbcTemplate(dsFBM); 
    } 
    
}