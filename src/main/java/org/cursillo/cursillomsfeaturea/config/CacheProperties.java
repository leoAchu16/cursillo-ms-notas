package org.cursillo.cursillomsfeaturea.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    // Si no se define un app.cache.default-ttl, se usarán 60 minutos por defecto
    private long defaultTtl = 60;

    // Mapea automáticamente notas-categoria, notas-detalle, etc.
    private Map<String, Long> ttl;
}