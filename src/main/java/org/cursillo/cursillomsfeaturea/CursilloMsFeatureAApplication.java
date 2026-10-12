package org.cursillo.cursillomsfeaturea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(servers = {@Server(url = "http://localhost:8084/api", description = "API Gateway")})
@SpringBootApplication
@EntityScan(basePackages = {"org.cursillo.commons.entities", "org.cursillo.cursillomsfeaturea"})
public class CursilloMsFeatureAApplication {

    public static void main(String[] args) {
        SpringApplication.run(CursilloMsFeatureAApplication.class, args);
    }

}