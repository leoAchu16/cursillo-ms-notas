package org.cursillo.cursillomsfeaturea.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;

@Slf4j
@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        byte[] bytes = Decoders.BASE64.decode(props.secret());
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe ser al menos de 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer idUsuario(Claims claims) {
        return claims.get(props.claim().user(), Integer.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> roles(Claims claims) {
        return claims.get(props.claim().roles(), List.class);
    }
}