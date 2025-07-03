package az.kb.mscore.service;

import az.kb.mscore.client.TransactionClient;
import az.kb.mscore.model.TransactionRequest;
import az.kb.mscore.model.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionClient transactionClient;

    public TransactionResponse getTransactionById(Long id) {
        return transactionClient.getTransactionById(id);
    }

    public List<TransactionResponse> getTransactionsByCustomerId(Long customerId) {
        return transactionClient.getCustomerTransactions(customerId);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionClient.getAllTransactions();
    }

    public TransactionResponse doTopup(TransactionRequest transactionRequest) {
        return transactionClient.doTopup(transactionRequest);
    }

    public TransactionResponse doPurchase(TransactionRequest transactionRequest) {
        return transactionClient.doPurchase(transactionRequest);
    }

    public TransactionResponse doRefund(TransactionRequest transactionRequest) {
        return transactionClient.doRefund(transactionRequest);
    }
}
