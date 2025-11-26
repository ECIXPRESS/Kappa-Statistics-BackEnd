package edu.dosw.Kappa_Stats_BackEnd.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kappa - Estadísticas API")
                        .version("1.0.0")
                        .description("""
                                Microservicio de estadísticas de ventas, productos más vendidos,
                                ingresos y reportes avanzados. Melo pa' producción.
                                """)
                        .contact(new Contact()
                                .name("Team Kappa")
                                .email("support@kappa.com")
                        )
                );
    }
}
