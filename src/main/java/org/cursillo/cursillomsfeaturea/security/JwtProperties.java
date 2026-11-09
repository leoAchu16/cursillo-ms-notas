package org.cursillo.cursillomsfeaturea.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotBlank String algorithm,
        @NotBlank String issuer,
        @Valid Claim claim) {

    public record Claim(@NotBlank String user, @NotBlank String roles) {}
}