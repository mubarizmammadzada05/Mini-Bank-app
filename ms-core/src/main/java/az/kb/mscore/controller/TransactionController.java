package az.kb.mscore.controller;

import az.kb.mscore.model.TransactionRequest;
import az.kb.mscore.model.TransactionResponse;
import az.kb.mscore.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @GetMapping("/{customerId}/customer")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(transactionService.getTransactionsByCustomerId(customerId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @PostMapping("/topup")
    public ResponseEntity<TransactionResponse> topupTransaction(@RequestBody TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.doTopup(transactionRequest));
    }

    @PostMapping("/purchase")
    public ResponseEntity<TransactionResponse> purchaseTransaction(@RequestBody TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.doPurchase(transactionRequest));
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refundTransaction(@RequestBody TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.doRefund(transactionRequest));
    }
}
