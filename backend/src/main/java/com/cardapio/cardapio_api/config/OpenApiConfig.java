package com.cardapio.cardapio_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Informações gerais da documentação Swagger (RF009) e o botão "Authorize",
 * que permite testar as rotas protegidas colando o token JWT.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API do Cardápio Digital")
                        .version("1.0")
                        .description("API REST do Cardápio Digital (Prática Extensionista VII). "
                                + "Rotas do cliente são públicas; as rotas de administração exigem "
                                + "login (POST /api/auth/login) e o token JWT no botão Authorize."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
