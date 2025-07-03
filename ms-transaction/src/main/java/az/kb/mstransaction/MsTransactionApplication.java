package az.kb.mstransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "az.kb.mstransaction.client")
public class MsTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsTransactionApplication.class, args);
    }

}
