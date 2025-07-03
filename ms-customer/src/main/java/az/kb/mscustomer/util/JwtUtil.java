package az.kb.mscustomer.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${security.secret-key}")
    private String secretKey;

    public boolean validateServiceToken(String token) {
        System.out.println("=== TOKEN VALIDATION DEBUG ===");
        System.out.println("Secret key length: " + secretKey.length());
        System.out.println("Secret key (first 10): " + secretKey.substring(0, Math.min(10, secretKey.length())));

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("Token parsed successfully!");
            System.out.println("Subject: " + claims.getSubject());
            System.out.println("Issuer: " + claims.getIssuer());
            System.out.println("Audience: " + claims.getAudience());
            System.out.println("Expiration: " + claims.getExpiration());
            System.out.println("Current time: " + new Date());

            String subject = claims.getSubject();
            boolean subjectValid = subject != null &&
                    (subject.startsWith("service:") ||
                            "ms-core".equals(subject) ||
                            "ms-transaction".equals(subject) ||
                            "ms-customer".equals(subject));
            boolean audienceValid = true;

            System.out.println("Subject valid: " + subjectValid);
            System.out.println("Audience valid: " + audienceValid);

            return subjectValid;

        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("Invalid signature: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Token validation error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getServiceName(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
