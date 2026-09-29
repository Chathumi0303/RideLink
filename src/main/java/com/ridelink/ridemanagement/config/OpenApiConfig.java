package com.ridelink.ridemanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI rideManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Ride Management Service API")
                        .version("1.0.0")
                        .description("Backend Microservice responsible for Ride lifecycle management, " +
                                "pickup/destination handling, driver assignment, and Fare/Payment completion integration. " +
                                "Developed for IT3130 - Application Development (Group Assignment: RideLink).")
                        .contact(new Contact()
                                .name("RideLink Backend Team - Member 3 (Ride Management Service)")
                                .email("ridelink-support@example.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token issued by RideLink Account Service (include roles: ROLE_PASSENGER, ROLE_DRIVER, or ROLE_ADMIN)")));
    }
}
