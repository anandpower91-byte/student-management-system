package com.example.demo;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentManagementAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Student Management System API")
                        .version("1.0")
                        .description(
                                "REST API for managing students, "
                                + "including student creation, "
                                + "updating, searching and deletion."
                        )
                        .contact(new Contact()
                                .name("Student Management System")
                        )
                );
    }
}