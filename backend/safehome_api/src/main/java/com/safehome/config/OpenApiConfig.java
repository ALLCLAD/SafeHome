package com.safehome.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Swagger / OpenAPI 3 pour la plateforme domotique SafeHome.
 * Active la documentation interactive et configure le schéma d'authentification
 * par Bearer Token JWT (bouton Authorize).
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI safeHomeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeHome Smart Home API")
                        .description("API REST domotique & passerelle IoT pour l'application mobile Flutter et la maquette 3D Web.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SafeHome Dev Team")
                                .email("contact@safehome.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                // Configuration du cadenas d'authentification Bearer JWT
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insérez le token JWT (sans le mot-clé 'Bearer ') obtenu via /api/auth/login")));
    }
}
