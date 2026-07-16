package miiiiiin.com.vinyler.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
http://localhost:8080/swagger-ui/index.html로 접속
 */
@OpenAPIDefinition(
        info =@Info(
                title ="Vinyler API Docs",
                description = "Vinyler의 API 문서입니다",
                version = "v1"
        )
)
@Configuration
@EnableAutoConfiguration
public class SwaggerConfig {

    private static final String BEARER_TOKEN_PREFIX = "Bearer";

    @Bean
    public OpenAPI commonAPI() {
        String securityJwtName = "jwt";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityJwtName);
        Components components = new Components()
                .addSecuritySchemes(securityJwtName, new SecurityScheme()
                        .name(securityJwtName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(BEARER_TOKEN_PREFIX)
                        .bearerFormat(securityJwtName));
        return new OpenAPI().components(components)
                .addSecurityItem(securityRequirement);

    }
}
