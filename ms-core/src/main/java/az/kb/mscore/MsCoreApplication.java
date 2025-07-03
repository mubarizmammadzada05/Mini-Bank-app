package az.kb.mscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsCoreApplication.class, args);
    }

}
