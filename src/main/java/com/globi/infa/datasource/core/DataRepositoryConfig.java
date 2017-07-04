package com.globi.infa.datasource.core;



import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.globi.infa.workflow.InfaWorkflow;
import com.globi.infa.workflow.PTPWorkflow;
import com.globi.metadata.sourcesystem.SourceSystem;

@Configuration
public class DataRepositoryConfig extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(InfaWorkflow.class);
        config.exposeIdsFor(PTPWorkflow.class);
        config.exposeIdsFor(SourceSystem.class);
    }
    
    @Override
    public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}