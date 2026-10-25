package org.cursillo.cursillomsfeaturea.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, CacheProperties cacheProperties) {

        // Uso del estándar actual para serialización JSON
        RedisSerializationContext.SerializationPair<Object> jsonSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(jsonSerializer)
                .entryTtl(Duration.ofMinutes(cacheProperties.getDefaultTtl()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configuracionesPorDominio = new HashMap<>();

        if (cacheProperties.getTtl() != null) {
            cacheProperties.getTtl().forEach((dominio, ttlMinutos) -> {
                RedisCacheConfiguration config = ttlMinutos <= 0
                        ? RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(jsonSerializer)
                        : defaultConfig.entryTtl(Duration.ofMinutes(ttlMinutos));

                configuracionesPorDominio.put(dominio, config);
            });
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configuracionesPorDominio)
                .build();
    }
}