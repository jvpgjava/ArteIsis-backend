package com.arteisis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI arteIsisOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Arte Isis API")
                        .description(
                                "REST API (catálogo público, autenticação JWT, admin). Importa `/v3/api-docs` no Postman ou usa a coleção em `docs/postman/`.")
                        .version("1.0.0"));
    }
}
