package az.kb.mstransaction.service;

import az.kb.mstransaction.client.CustomerClient;
import az.kb.mstransaction.entity.Transaction;
import az.kb.mstransaction.entity.TransactionStatusHistory;
import az.kb.mstransaction.enums.TransactionStatus;
import az.kb.mstransaction.enums.TransactionType;
import az.kb.mstransaction.exception.NotFoundException;
import az.kb.mstransaction.model.TransactionRequest;
import az.kb.mstransaction.model.UpdateBalanceRequest;
import az.kb.mstransaction.repository.HistoryRepository;
import az.kb.mstransaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static az.kb.mstransaction.constant.ErrorMessage.TRANSACTION_NOT_FOUND;
import static az.kb.mstransaction.enums.TransactionStatus.FAILED;
import static az.kb.mstransaction.enums.TransactionStatus.PENDING;
import static az.kb.mstransaction.enums.TransactionStatus.SUCCESS;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final HistoryRepository historyRepository;
    private final CustomerClient customerClient;

    @Transactional
    public Transaction topUp(TransactionRequest request) {
        validateAmount(request.getAmount());
        return processTransaction(request, TransactionType.TOP_UP);
    }

    @Transactional
    public Transaction purchase(TransactionRequest request) {
        validateAmount(request.getAmount());
        return processTransaction(request, TransactionType.PURCHASE);
    }

    @Transactional
    public Transaction refund(TransactionRequest request) {
        Transaction original = transactionRepository.findByIdAndCustomerId(
                request.getRelatedTransactionId(), request.getCustomerId()
        ).orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND, request.getRelatedTransactionId()));
        validateAmount(request.getAmount());

        if (request.getAmount().compareTo(original.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot be greater than original transaction amount");
        }

        return processTransaction(request, TransactionType.REFUND);
    }

    private Transaction processTransaction(TransactionRequest request, TransactionType type) {
        Transaction transaction = createAndSaveTransaction(request, type, PENDING);
        saveStatusHistory(transaction.getId(), PENDING);

        try {
            BigDecimal adjustedAmount = calculateAmountByType(request, type);
            UpdateBalanceRequest updateBalanceRequest = UpdateBalanceRequest.builder()
                    .amount(adjustedAmount)
                    .build();
            customerClient.updateBalance(request.getCustomerId(), updateBalanceRequest);
            updateTransactionStatus(transaction, SUCCESS);
        } catch (Exception e) {
            updateTransactionStatus(transaction, FAILED);
            throw new RuntimeException("Error updating transaction", e);
        }

        return transaction;
    }

    private BigDecimal calculateAmountByType(TransactionRequest request, TransactionType type) {
        BigDecimal amount = request.getAmount();

        if (type == TransactionType.TOP_UP) {
            return amount;
        }

        if (type == TransactionType.PURCHASE) {
            return amount.negate();
        }

        if (type == TransactionType.REFUND) {
            Transaction original = transactionRepository.findByIdAndCustomerId(
                    request.getRelatedTransactionId(), request.getCustomerId()
            ).orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND, request.getRelatedTransactionId()));

            if (original.getType() == TransactionType.TOP_UP) {
                return amount.negate();
            } else if (original.getType() == TransactionType.PURCHASE) {
                return amount;
            }
        }

        throw new IllegalArgumentException("Unsupported transaction type: " + type);
    }


    private Transaction createAndSaveTransaction(TransactionRequest request, TransactionType type, TransactionStatus status) {
        Transaction transaction = Transaction.builder()
                .customerId(request.getCustomerId())
                .type(type)
                .amount(request.getAmount())
                .status(status)
                .relatedTransactionId(request.getRelatedTransactionId())
                .build();
        return transactionRepository.save(transaction);
    }

    private void updateTransactionStatus(Transaction transaction, TransactionStatus status) {
        transaction.setStatus(status);
        transactionRepository.save(transaction);
        saveStatusHistory(transaction.getId(), status);
    }

    private void saveStatusHistory(Long transactionId, TransactionStatus status) {
        TransactionStatusHistory history = TransactionStatusHistory.builder()
                .transactionId(transactionId)
                .status(status)
                .changedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND, id));
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionByCustomerId(Long customerId) {
        return transactionRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException(TRANSACTION_NOT_FOUND, customerId));
    }
}

