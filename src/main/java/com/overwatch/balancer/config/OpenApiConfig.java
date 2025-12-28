package com.overwatch.balancer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MixBalancer API")
                        .description("""
                                Team balancing API for Overwatch 2 custom games.
                                
                                ## Features
                                - **Player Management**: Register players with SR for each role
                                - **Lobby System**: Create and manage game lobbies
                                - **Smart Balancing**: Advanced algorithm for fair team distribution
                                - **Map Selection**: Random map selection from all OW2 maps
                                - **Queue Management**: Priority queue for players who missed games
                                
                                ## Balance Algorithm
                                The balancer prioritizes:
                                1. **Tank SR similarity** - Tanks are matched as closely as possible
                                2. **Overall team balance** - Total SR is equalized
                                3. **Role compensation** - DPS/Support can offset each other
                                4. **Priority queue** - Players who skipped games get priority
                                
                                ## Integration
                                Use this API to integrate balancing into:
                                - Discord bots
                                - Web applications
                                - Custom tools
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MixBalancer")
                                .url("https://github.com/kuber4s/MixBalancer"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")
                ));
    }

}
