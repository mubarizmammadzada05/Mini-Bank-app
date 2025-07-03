package az.kb.mscustomer.config;

import az.kb.mscustomer.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            String method = request.getMethod();

            System.out.println("=== JWT FILTER START ===");
            System.out.println("Path: " + path);
            System.out.println("Method: " + method);

            String authHeader = request.getHeader("Authorization");
            System.out.println("Auth Header: " + authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("No Bearer token found");
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            System.out.println("Token extracted: " + token.substring(0, 20) + "...");

            if (jwtService.validateServiceToken(token)) {
                System.out.println("Token is valid");

                String username = jwtService.getServiceName(token);
                System.out.println("Username from token: " + username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    List<GrantedAuthority> authorities = new ArrayList<>();

                    if ("ms-transaction".equals(username) ||
                            "ms-customer".equals(username) ||
                            "ms-core".equals(username) ||
                            "ms-notification".equals(username)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_SERVICE"));
                        System.out.println("Assigned ROLE_SERVICE to: " + username);
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("Authentication set for user: " + username + " with authorities: " + authorities);
                }
            } else {
                System.out.println("Token is invalid");
            }

            System.out.println("=== JWT FILTER END - Proceeding to controller ===");
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("=== JWT FILTER EXCEPTION ===");
            System.out.println("Exception: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();

            filterChain.doFilter(request, response);
        }
    }

}