package com.hemopharm.backend.conf;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hemopharmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hemopharm API")
                        .version("1.0")
                        .description("Blood test analysis API"));
    }
}
