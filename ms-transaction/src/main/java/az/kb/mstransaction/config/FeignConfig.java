package az.kb.mstransaction.config;

import az.kb.mstransaction.util.JwtUtil;
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
            String token = jwtUtil.generateServiceToken("ms-transaction");
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}
