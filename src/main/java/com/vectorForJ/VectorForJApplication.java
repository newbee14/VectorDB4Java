package com.vectorForJ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import com.vectorForJ.constants.ApplicationConstants.Api;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = Api.API_TITLE,
        version = Api.API_VERSION,
        description = Api.API_DESCRIPTION
    )
)
public class VectorForJApplication {
    public static void main(String[] args) {
        SpringApplication.run(VectorForJApplication.class, args);
    }
} 