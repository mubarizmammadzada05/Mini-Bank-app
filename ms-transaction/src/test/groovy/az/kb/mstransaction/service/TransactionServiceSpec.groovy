package az.kb.mstransaction.service

import az.kb.mstransaction.client.CustomerClient
import az.kb.mstransaction.entity.Transaction
import az.kb.mstransaction.entity.TransactionStatusHistory
import az.kb.mstransaction.enums.TransactionStatus
import az.kb.mstransaction.enums.TransactionType
import az.kb.mstransaction.exception.NotFoundException
import az.kb.mstransaction.model.TransactionRequest
import az.kb.mstransaction.model.UpdateBalanceRequest
import az.kb.mstransaction.repository.HistoryRepository
import az.kb.mstransaction.repository.TransactionRepository
import spock.lang.Specification
import spock.lang.Subject

import java.math.BigDecimal
import java.time.LocalDateTime

class TransactionServiceSpec extends Specification {

    def transactionRepository = Mock(TransactionRepository)
    def historyRepository = Mock(HistoryRepository)
    def customerClient = Mock(CustomerClient)

    @Subject
    TransactionService transactionService = new TransactionService(
            transactionRepository, historyRepository, customerClient
    )

    def "topUp should create successful TOP_UP transaction"() {
        given: "a valid top up request"
        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("100.00"))
                .build()

        def savedTransaction = Transaction.builder()
                .id(123L)
                .customerId(1L)
                .type(TransactionType.TOP_UP)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.PENDING)
                .build()

        def successTransaction = Transaction.builder()
                .id(123L)
                .customerId(1L)
                .type(TransactionType.TOP_UP)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.SUCCESS)
                .build()

        when: "topUp is called"
        def result = transactionService.topUp(request)

        then: "transaction is saved with PENDING status"
        1 * transactionRepository.save(_ as Transaction) >> { Transaction tx ->
            assert tx.customerId == 1L
            assert tx.type == TransactionType.TOP_UP
            assert tx.amount == new BigDecimal("100.00")
            assert tx.status == TransactionStatus.PENDING
            return savedTransaction
        }

        and: "status history is saved for PENDING"
        1 * historyRepository.save(_ as TransactionStatusHistory) >> { TransactionStatusHistory history ->
            assert history.transactionId == 123L
            assert history.status == TransactionStatus.PENDING
            return history
        }

        and: "customer balance is updated with positive amount"
        1 * customerClient.updateBalance(1L, _ as UpdateBalanceRequest) >> { Long customerId, UpdateBalanceRequest updateRequest ->
            assert updateRequest.amount == new BigDecimal("100.00")
        }

        and: "transaction status is updated to SUCCESS"
        1 * transactionRepository.save(_ as Transaction) >> { Transaction tx ->
            assert tx.status == TransactionStatus.SUCCESS
            return successTransaction
        }

        and: "status history is saved for SUCCESS"
        1 * historyRepository.save(_ as TransactionStatusHistory) >> { TransactionStatusHistory history ->
            assert history.transactionId == 123L
            assert history.status == TransactionStatus.SUCCESS
            return history
        }

        and: "result is returned"
        result.type == TransactionType.TOP_UP
        result.status == TransactionStatus.SUCCESS
    }

//    def "topUp should fail when customer client throws exception"() {
//        given: "a valid top up request"
//        def request = TransactionRequest.builder()
//                .customerId(1L)
//                .amount(new BigDecimal("100.00"))
//                .build()
//
//        def savedTransaction = Transaction.builder()
//                .id(123L)
//                .customerId(1L)
//                .type(TransactionType.TOP_UP)
//                .amount(new BigDecimal("100.00"))
//                .status(TransactionStatus.PENDING)
//                .build()
//
//        when: "topUp is called and customer client fails"
//        def result = transactionService.topUp(request)
//
//        then: "transaction is saved with PENDING status"
//        1 * transactionRepository.save(_ as Transaction) >> savedTransaction
//
//        and: "status history is saved for PENDING"
//        1 * historyRepository.save(_ as TransactionStatusHistory)
//
//        and: "customer client throws exception"
//        1 * customerClient.updateBalance(1L, _ as UpdateBalanceRequest) >> {
//            throw new RuntimeException("Customer service unavailable")
//        }
//
//        and: "transaction status is updated to FAILED"
//        1 * transactionRepository.save(_ as Transaction) >> { Transaction tx ->
//            assert tx.status == TransactionStatus.FAILED
//            return tx
//        }
//
//        and: "status history is saved for FAILED"
//        1 * historyRepository.save(_ as TransactionStatusHistory) >> { TransactionStatusHistory history ->
//            assert history.status == TransactionStatus.FAILED
//            return history
//        }
//
//        and: "transaction is returned with FAILED status"
//        result.status == TransactionStatus.FAILED
//    }

    def "purchase should create successful PURCHASE transaction with negative amount"() {
        given: "a valid purchase request"
        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("50.00"))
                .build()

        def savedTransaction = Transaction.builder()
                .id(124L)
                .customerId(1L)
                .type(TransactionType.PURCHASE)
                .amount(new BigDecimal("50.00"))
                .status(TransactionStatus.PENDING)
                .build()

        when: "purchase is called"
        def result = transactionService.purchase(request)

        then: "transaction is saved"
        1 * transactionRepository.save(_ as Transaction) >> savedTransaction

        and: "status history is saved for PENDING"
        1 * historyRepository.save(_ as TransactionStatusHistory)

        and: "customer balance is updated with negative amount"
        1 * customerClient.updateBalance(1L, _ as UpdateBalanceRequest) >> { Long customerId, UpdateBalanceRequest updateRequest ->
            assert updateRequest.amount == new BigDecimal("-50.00")
        }

        and: "transaction status is updated to SUCCESS"
        1 * transactionRepository.save(_ as Transaction) >> { Transaction tx ->
            assert tx.status == TransactionStatus.SUCCESS
            return tx
        }

        and: "status history is saved for SUCCESS"
        1 * historyRepository.save(_ as TransactionStatusHistory)

        and: "result has correct type"
        result.type == TransactionType.PURCHASE
    }

    def "refund should create successful REFUND transaction for TOP_UP with negative amount"() {
        given: "a refund request for a TOP_UP transaction"
        def originalTransaction = Transaction.builder()
                .id(100L)
                .customerId(1L)
                .type(TransactionType.TOP_UP)
                .amount(new BigDecimal("200.00"))
                .status(TransactionStatus.SUCCESS)
                .build()

        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("50.00"))
                .relatedTransactionId(100L)
                .build()

        def savedRefundTransaction = Transaction.builder()
                .id(125L)
                .customerId(1L)
                .type(TransactionType.REFUND)
                .amount(new BigDecimal("50.00"))
                .status(TransactionStatus.PENDING)
                .relatedTransactionId(100L)
                .build()

        when: "refund is called"
        def result = transactionService.refund(request)

        then: "original transaction is found"
        1 * transactionRepository.findByIdAndCustomerId(100L, 1L) >> Optional.of(originalTransaction)

        and: "refund transaction is saved"
        1 * transactionRepository.save(_ as Transaction) >> savedRefundTransaction

        and: "status history is saved for PENDING"
        1 * historyRepository.save(_ as TransactionStatusHistory)

        and: "original transaction is found again for amount calculation"
        1 * transactionRepository.findByIdAndCustomerId(100L, 1L) >> Optional.of(originalTransaction)

        and: "customer balance is updated with negative amount (refunding TOP_UP)"
        1 * customerClient.updateBalance(1L, _ as UpdateBalanceRequest) >> { Long customerId, UpdateBalanceRequest updateRequest ->
            assert updateRequest.amount == new BigDecimal("-50.00")
        }

        and: "transaction status is updated to SUCCESS"
        1 * transactionRepository.save(_ as Transaction) >> { Transaction tx ->
            assert tx.status == TransactionStatus.SUCCESS
            return tx
        }

        and: "status history is saved for SUCCESS"
        1 * historyRepository.save(_ as TransactionStatusHistory)

        and: "result has correct type"
        result.type == TransactionType.REFUND
    }

    def "refund should create successful REFUND transaction for PURCHASE with positive amount"() {
        given: "a refund request for a PURCHASE transaction"
        def originalTransaction = Transaction.builder()
                .id(101L)
                .customerId(1L)
                .type(TransactionType.PURCHASE)
                .amount(new BigDecimal("30.00"))
                .status(TransactionStatus.SUCCESS)
                .build()

        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("30.00"))
                .relatedTransactionId(101L)
                .build()

        def savedRefundTransaction = Transaction.builder()
                .id(126L)
                .customerId(1L)
                .type(TransactionType.REFUND)
                .amount(new BigDecimal("30.00"))
                .status(TransactionStatus.PENDING)
                .relatedTransactionId(101L)
                .build()

        when: "refund is called"
        def result = transactionService.refund(request)

        then: "original transaction is found"
        1 * transactionRepository.findByIdAndCustomerId(101L, 1L) >> Optional.of(originalTransaction)

        and: "refund transaction is saved"
        1 * transactionRepository.save(_ as Transaction) >> savedRefundTransaction

        and: "status history is saved for PENDING"
        1 * historyRepository.save(_ as TransactionStatusHistory)

        and: "original transaction is found again for amount calculation"
        1 * transactionRepository.findByIdAndCustomerId(101L, 1L) >> Optional.of(originalTransaction)

        and: "customer balance is updated with positive amount (refunding PURCHASE)"
        1 * customerClient.updateBalance(1L, _ as UpdateBalanceRequest) >> { Long customerId, UpdateBalanceRequest updateRequest ->
            assert updateRequest.amount == new BigDecimal("30.00")
        }

        and: "transaction status is updated to SUCCESS"
        1 * transactionRepository.save(_ as Transaction) >> { Transaction tx ->
            assert tx.status == TransactionStatus.SUCCESS
            return tx
        }

        and: "status history is saved for SUCCESS"
        1 * historyRepository.save(_ as TransactionStatusHistory)
    }

    def "refund should throw NotFoundException when original transaction not found"() {
        given: "a refund request with non-existent related transaction"
        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("50.00"))
                .relatedTransactionId(999L)
                .build()

        when: "refund is called"
        transactionService.refund(request)

        then: "original transaction is not found"
        1 * transactionRepository.findByIdAndCustomerId(999L, 1L) >> Optional.empty()

        and: "NotFoundException is thrown"
        thrown(NotFoundException)
    }

    def "refund should throw IllegalArgumentException when refund amount is greater than original amount"() {
        given: "a refund request with amount greater than original"
        def originalTransaction = Transaction.builder()
                .id(100L)
                .customerId(1L)
                .type(TransactionType.TOP_UP)
                .amount(new BigDecimal("50.00"))
                .status(TransactionStatus.SUCCESS)
                .build()

        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("100.00"))
                .relatedTransactionId(100L)
                .build()

        when: "refund is called"
        transactionService.refund(request)

        then: "original transaction is found"
        1 * transactionRepository.findByIdAndCustomerId(100L, 1L) >> Optional.of(originalTransaction)

        and: "IllegalArgumentException is thrown"
        def ex = thrown(IllegalArgumentException)
        ex.message == "Refund amount cannot be greater than original transaction amount"
    }

    def "getAllTransactions should return all transactions"() {
        given: "transactions exist in repository"
        def transactions = [
                Transaction.builder().id(1L).build(),
                Transaction.builder().id(2L).build()
        ]

        when: "getAllTransactions is called"
        def result = transactionService.getAllTransactions()

        then: "repository findAll is called"
        1 * transactionRepository.findAll() >> transactions

        and: "all transactions are returned"
        result == transactions
        result.size() == 2
    }

    def "getTransactionById should return transaction when found"() {
        given: "a transaction exists"
        def transaction = Transaction.builder()
                .id(1L)
                .customerId(1L)
                .build()

        when: "getTransactionById is called"
        def result = transactionService.getTransactionById(1L)

        then: "repository findById is called"
        1 * transactionRepository.findById(1L) >> Optional.of(transaction)

        and: "transaction is returned"
        result == transaction
    }

    def "getTransactionById should throw NotFoundException when not found"() {
        when: "getTransactionById is called with non-existent id"
        transactionService.getTransactionById(999L)

        then: "repository findById is called"
        1 * transactionRepository.findById(999L) >> Optional.empty()

        and: "NotFoundException is thrown"
        thrown(NotFoundException)
    }


    def "getTransactionByCustomerId should return transaction when found"() {
        given: "a transaction exists for customer"
        def transaction = Transaction.builder()
                .id(1L)
                .customerId(1L)
                .build()
        def transactionList = [transaction]

        when: "getTransactionByCustomerId is called"
        def result = transactionService.getTransactionByCustomerId(1L)

        then: "repository findByCustomerId is called"
        1 * transactionRepository.findByCustomerId(1L) >> Optional.of(transactionList)

        and: "transaction list is returned"
        result == transactionList
        result.size() == 1
        result[0] == transaction
    }

    def "getTransactionByCustomerId should throw NotFoundException when not found"() {
        when: "getTransactionByCustomerId is called with non-existent customer"
        transactionService.getTransactionByCustomerId(999L)

        then: "repository findByCustomerId is called"
        1 * transactionRepository.findByCustomerId(999L) >> Optional.empty()

        and: "NotFoundException is thrown"
        thrown(NotFoundException)
    }

    def "validateAmount should throw IllegalArgumentException for invalid amounts"() {
        given: "a transaction request with invalid amount"
        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(amount)
                .build()

        when: "topUp is called with invalid amount"
        transactionService.topUp(request)

        then: "IllegalArgumentException is thrown"
        def ex = thrown(IllegalArgumentException)
        ex.message == "Amount must be positive"

        where:
        amount << [null, BigDecimal.ZERO, new BigDecimal("-10.00")]
    }

    def "calculateAmountByType should return correct amounts for different transaction types"() {
        given: "transaction request and original transaction (for refund cases)"
        def request = TransactionRequest.builder()
                .customerId(1L)
                .amount(new BigDecimal("100.00"))
                .relatedTransactionId(originalTransactionId)
                .build()

        and: "original transaction exists when needed"
        if (originalTransaction) {
            transactionRepository.findByIdAndCustomerId(originalTransactionId, 1L) >> Optional.of(originalTransaction)
        }

        when: "processTransaction is called"
        def savedTransaction = Transaction.builder()
                .id(123L)
                .customerId(1L)
                .type(transactionType)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.PENDING)
                .build()

        transactionRepository.save(_ as Transaction) >> savedTransaction
        historyRepository.save(_ as TransactionStatusHistory) >> Mock(TransactionStatusHistory)
        customerClient.updateBalance(1L, _ as UpdateBalanceRequest) >> { Long customerId, UpdateBalanceRequest updateRequest ->
            assert updateRequest.amount == expectedAmount
        }
        transactionRepository.save(_ as Transaction) >> savedTransaction
        historyRepository.save(_ as TransactionStatusHistory) >> Mock(TransactionStatusHistory)

        transactionService.processTransaction(request, transactionType)

        then: "correct amount is calculated"

        where:
        transactionType         | originalTransactionId | originalTransaction                                                                           | expectedAmount
        TransactionType.TOP_UP  | null                  | null                                                                                          | new BigDecimal("100.00")
        TransactionType.PURCHASE| null                  | null                                                                                          | new BigDecimal("-100.00")
        TransactionType.REFUND  | 1L                    | Transaction.builder().id(1L).type(TransactionType.TOP_UP).amount(new BigDecimal("200.00")).build()    | new BigDecimal("-100.00")
        TransactionType.REFUND  | 2L                    | Transaction.builder().id(2L).type(TransactionType.PURCHASE).amount(new BigDecimal("200.00")).build() | new BigDecimal("100.00")
    }
}