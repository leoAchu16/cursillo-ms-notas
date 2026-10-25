package org.cursillo.cursillomsfeaturea.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay token o no empieza con Bearer, continuamos (Spring Security lo bloqueará luego si es requerido)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            // Verifica la firma y expiración del JWT
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            String username = claims.getSubject();

            // IMPORTANTE: Debes acordar con tu compañero cómo se llama este campo en el JWT ("roles", "rol", etc.)
            List<String> roles = claims.get("roles", List.class);

            List<SimpleGrantedAuthority> authorities = roles != null ?
                    roles.stream().map(SimpleGrantedAuthority::new).toList() : List.of();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            // Si el token expira o la firma es inválida, se limpia el contexto y devolverá 403 Forbidden
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}