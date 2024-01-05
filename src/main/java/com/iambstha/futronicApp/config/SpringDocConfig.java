package com.iambstha.futronicApp.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;

public class SpringDocConfig {
	
    @Bean
    public GroupedOpenApi controllerApi() {
        return GroupedOpenApi.builder()
                .group("fingerprint-api")
                .packagesToScan("com.iambstha.futronicApp")
                .build();
    }



}
