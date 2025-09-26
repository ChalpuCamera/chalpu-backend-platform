package com.example.chalpuplatform.oauth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.client.RestTemplate;
import java.lang.reflect.Method;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "OAuth2 Module API",
        version = "1.0",
        description = "API Documentation for OAuth2 Module",
        contact = @Contact(
            name = "OAuth2 Support",
            email = "support@example.com",
            url = "https://github.com/yourusername/oauth2-module"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            url = "/",
            description = "Local Server"
        )
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Authorization header using Bearer scheme",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public GroupedOpenApi ownerApi() {
        return GroupedOpenApi.builder()
            .group("1. 사장님 API")
            .pathsToMatch("/api/**")
            .packagesToScan("com.example.chalpuplatform")
            .addOpenApiMethodFilter(method -> hasRole(method, "OWNER"))
            .build();
    }

    @Bean
    public GroupedOpenApi customerApi() {
        return GroupedOpenApi.builder()
            .group("2. 손님 API")
            .pathsToMatch("/api/**")
            .packagesToScan("com.example.chalpuplatform")
            .addOpenApiMethodFilter(method -> hasRole(method, "CUSTOMER"))
            .build();
    }

    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
            .group("3. 공통 API")
            .pathsToMatch("/api/**")
            .packagesToScan("com.example.chalpuplatform")
            .addOpenApiMethodFilter(method -> !method.isAnnotationPresent(PreAuthorize.class))
            .build();
    }

    private boolean hasRole(Method method, String role) {
        if (method.isAnnotationPresent(PreAuthorize.class)) {
            PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
            return preAuthorize.value().contains(role);
        }
        return false;
    }
}