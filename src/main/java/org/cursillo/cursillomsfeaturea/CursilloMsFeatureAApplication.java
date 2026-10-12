package org.cursillo.cursillomsfeaturea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = "org.cursillo")
@EntityScan(basePackages = {"org.cursillo.commons.entities", "org.cursillo.cursillomsfeaturea"})
public class CursilloMsFeatureAApplication {

    public static void main(String[] args) {
        SpringApplication.run(CursilloMsFeatureAApplication.class, args);
    }

}