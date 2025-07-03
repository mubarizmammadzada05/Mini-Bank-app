package az.kb.mscore.client;

import az.kb.mscore.config.FeignConfig;
import az.kb.mscore.model.CustomerCreateRequestDto;
import az.kb.mscore.model.CustomerResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-customer", url = "${client.ms-customer.url}",configuration = FeignConfig.class)
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{id}")
    CustomerResponseDto getCustomer(@PathVariable("id") Long customerId);

    @PostMapping("/api/v1/customers")
    CustomerResponseDto createCustomer(@RequestBody CustomerCreateRequestDto request);
}