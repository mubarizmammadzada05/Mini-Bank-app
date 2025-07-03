package az.kb.mscore.client;

import az.kb.mscore.model.TransactionRequest;
import az.kb.mscore.model.TransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-transaction", url = "${client.ms-transaction.url}")
public interface TransactionClient {

    @PostMapping("/api/v1/transactions/topup")
    TransactionResponse doTopup(@RequestBody TransactionRequest request);

    @PostMapping("/api/v1/transactions/purchase")
    TransactionResponse doPurchase(@RequestBody TransactionRequest request);

    @PostMapping("/api/v1/transactions/refund")
    TransactionResponse doRefund(@RequestBody TransactionRequest request);

    @GetMapping("/api/v1/transactions")
    List<TransactionResponse> getAllTransactions();

    @GetMapping("/api/v1/transactions/{customerId}/by-customer")
    List<TransactionResponse> getCustomerTransactions(@PathVariable Long customerId);

    @GetMapping("/api/v1/transactions//{id}")
    TransactionResponse getTransactionById(@PathVariable Long id);

}