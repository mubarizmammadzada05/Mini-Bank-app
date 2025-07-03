package az.kb.mstransaction.controller;

import az.kb.mstransaction.entity.Transaction;
import az.kb.mstransaction.model.TransactionRequest;
import az.kb.mstransaction.service.TransactionService;
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

    @PostMapping("/topup")
    public ResponseEntity<Transaction> topUp(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.topUp(request));
    }

    @PostMapping("/purchase")
    public ResponseEntity<Transaction> purchase(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.purchase(request));
    }

    @PostMapping("/refund")
    public ResponseEntity<Transaction> refund(@RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.refund(request));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{customerId}/by-customer")
    public ResponseEntity<List<Transaction>> getCustomerTransactions(@PathVariable Long customerId) {
        return ResponseEntity.ok(transactionService.getTransactionByCustomerId(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

}
