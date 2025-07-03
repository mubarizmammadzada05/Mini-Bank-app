package az.kb.mscore.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${security.secret-key}")
    private String secret;

    @Value("${security.expiration}")
    private long expirationTime;


    public String generateServiceToken(String serviceName) {
        return Jwts.builder()
                .setSubject(serviceName)
                .setIssuer(serviceName)
                .setAudience("service")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

}
