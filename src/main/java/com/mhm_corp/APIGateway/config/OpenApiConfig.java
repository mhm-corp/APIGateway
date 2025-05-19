package com.mhm_corp.APIGateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(Environment environment) {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway Service")
                        .description("Microservice that functions as an API gateway")
                        .version("1.0.0"));
    }


}
