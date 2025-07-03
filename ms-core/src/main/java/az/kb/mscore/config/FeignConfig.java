package az.kb.mscore.config;

import az.kb.mscore.util.JwtUtil;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return requestTemplate -> {
            String token = jwtUtil.generateServiceToken("ms-core");
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}
