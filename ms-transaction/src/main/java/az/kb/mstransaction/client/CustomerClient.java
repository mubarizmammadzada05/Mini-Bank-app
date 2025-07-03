package az.kb.mstransaction.client;

import az.kb.mstransaction.model.UpdateBalanceRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "ms-customer", url = "${client.ms-customer.url}")
public interface CustomerClient {

    @PutMapping("/api/v1/customers/{id}/balance")
    void updateBalance(@PathVariable("id") Long customerId, @RequestBody UpdateBalanceRequest request);
}
