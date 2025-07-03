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
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            boolean subjectValid = subject != null &&
                    (subject.startsWith("service:") ||
                            "ms-core".equals(subject) ||
                            "ms-transaction".equals(subject) ||
                            "ms-customer".equals(subject));
            boolean audienceValid = true;

            return subjectValid;

        } catch (ExpiredJwtException e) {
            return false;
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
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
