package com.vectorForJ.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI 3.0 documentation (Swagger).
 */
@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI vectorForJOpenAPI() {
        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Development server");

        Contact contact = new Contact()
                .name("VectorForJ Team")
                .email("contact@vectorforj.com")
                .url("https://github.com/yourusername/vectorForJ");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("VectorForJ API")
                .version("1.0.0")
                .contact(contact)
                .description("API documentation for VectorForJ - An in-memory vector database")
                .termsOfService("https://github.com/yourusername/vectorForJ")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
} 