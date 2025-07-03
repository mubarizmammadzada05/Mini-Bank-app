package az.kb.mscore.controller


import az.kb.mscore.model.TransactionRequest
import az.kb.mscore.model.TransactionResponse
import az.kb.mscore.enums.TransactionStatus
import az.kb.mscore.enums.TransactionType
import az.kb.mscore.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class TransactionControllerSpec extends Specification {

    TransactionController transactionController
    TransactionService transactionService = Mock()

    def setup() {
        transactionController = new TransactionController(transactionService)
    }

    def "getTransactionById should return transaction when found"() {
        given: "a transaction ID"
        def transactionId = 1L
        def expectedResponse = TransactionResponse.builder()
                .id(transactionId)
                .customerId(100L)
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("50.00"))
                .relatedTransactionId(null)
                .build()

        when: "getTransactionById is called"
        ResponseEntity<TransactionResponse> result = transactionController.getTransactionById(transactionId)

        then: "service is called and transaction is returned"
        1 * transactionService.getTransactionById(transactionId) >> expectedResponse
        result.statusCode == HttpStatus.OK
        result.body == expectedResponse
        result.body.id == transactionId
        result.body.customerId == 100L
        result.body.type == TransactionType.TOP_UP
        result.body.status == TransactionStatus.SUCCESS
        result.body.amount == new BigDecimal("50.00")
    }

    def "getTransactionsByCustomerId should return list of transactions for customer"() {
        given: "a customer ID"
        def customerId = 100L
        def transaction1 = TransactionResponse.builder()
                .id(1L)
                .customerId(customerId)
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("50.00"))
                .relatedTransactionId(null)
                .build()

        def transaction2 = TransactionResponse.builder()
                .id(2L)
                .customerId(customerId)
                .type(TransactionType.PURCHASE)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("25.00"))
                .relatedTransactionId(null)
                .build()

        def expectedTransactions = [transaction1, transaction2]

        when: "getTransactionsByCustomerId is called"
        ResponseEntity<List<TransactionResponse>> result = transactionController.getTransactionsByCustomerId(customerId)

        then: "service is called and transactions are returned"
        1 * transactionService.getTransactionsByCustomerId(customerId) >> expectedTransactions
        result.statusCode == HttpStatus.OK
        result.body == expectedTransactions
        result.body.size() == 2
        result.body[0].customerId == customerId
        result.body[1].customerId == customerId
    }

    def "getTransactionsByCustomerId should return empty list when no transactions found"() {
        given: "a customer ID with no transactions"
        def customerId = 999L
        def expectedTransactions = []

        when: "getTransactionsByCustomerId is called"
        ResponseEntity<List<TransactionResponse>> result = transactionController.getTransactionsByCustomerId(customerId)

        then: "service is called and empty list is returned"
        1 * transactionService.getTransactionsByCustomerId(customerId) >> expectedTransactions
        result.statusCode == HttpStatus.OK
        result.body == expectedTransactions
        result.body.size() == 0
    }

    def "getAllTransactions should return all transactions"() {
        given: "multiple transactions exist"
        def transaction1 = TransactionResponse.builder()
                .id(1L)
                .customerId(100L)
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("50.00"))
                .relatedTransactionId(null)
                .build()

        def transaction2 = TransactionResponse.builder()
                .id(2L)
                .customerId(200L)
                .type(TransactionType.PURCHASE)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("25.00"))
                .relatedTransactionId(null)
                .build()

        def expectedTransactions = [transaction1, transaction2]

        when: "getAllTransactions is called"
        ResponseEntity<List<TransactionResponse>> result = transactionController.getAllTransactions()

        then: "service is called and all transactions are returned"
        1 * transactionService.getAllTransactions() >> expectedTransactions
        result.statusCode == HttpStatus.OK
        result.body == expectedTransactions
        result.body.size() == 2
    }

    def "getAllTransactions should return empty list when no transactions exist"() {
        given: "no transactions exist"
        def expectedTransactions = []

        when: "getAllTransactions is called"
        ResponseEntity<List<TransactionResponse>> result = transactionController.getAllTransactions()

        then: "service is called and empty list is returned"
        1 * transactionService.getAllTransactions() >> expectedTransactions
        result.statusCode == HttpStatus.OK
        result.body == expectedTransactions
        result.body.size() == 0
    }

    def "topupTransaction should process topup successfully"() {
        given: "a valid topup request"
        def transactionRequest = TransactionRequest.builder()
                .customerId(100L)
                .amount(new BigDecimal("100.00"))
                .relatedTransactionId(null)
                .build()

        def expectedResponse = TransactionResponse.builder()
                .id(1L)
                .customerId(100L)
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("100.00"))
                .relatedTransactionId(null)
                .build()

        when: "topupTransaction is called"
        ResponseEntity<TransactionResponse> result = transactionController.topupTransaction(transactionRequest)

        then: "service is called and topup is processed"
        1 * transactionService.doTopup(transactionRequest) >> expectedResponse
        result.statusCode == HttpStatus.OK
        result.body == expectedResponse
        result.body.type == TransactionType.TOP_UP
        result.body.status == TransactionStatus.SUCCESS
        result.body.amount == new BigDecimal("100.00")
    }

    def "purchaseTransaction should process purchase successfully"() {
        given: "a valid purchase request"
        def transactionRequest = TransactionRequest.builder()
                .customerId(100L)
                .amount(new BigDecimal("50.00"))
                .relatedTransactionId(null)
                .build()

        def expectedResponse = TransactionResponse.builder()
                .id(2L)
                .customerId(100L)
                .type(TransactionType.PURCHASE)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("50.00"))
                .relatedTransactionId(null)
                .build()

        when: "purchaseTransaction is called"
        ResponseEntity<TransactionResponse> result = transactionController.purchaseTransaction(transactionRequest)

        then: "service is called and purchase is processed"
        1 * transactionService.doPurchase(transactionRequest) >> expectedResponse
        result.statusCode == HttpStatus.OK
        result.body == expectedResponse
        result.body.type == TransactionType.PURCHASE
        result.body.status == TransactionStatus.SUCCESS
        result.body.amount == new BigDecimal("50.00")
    }

    def "refundTransaction should process refund successfully"() {
        given: "a valid refund request"
        def transactionRequest = TransactionRequest.builder()
                .customerId(100L)
                .amount(new BigDecimal("30.00"))
                .relatedTransactionId(2L)
                .build()

        def expectedResponse = TransactionResponse.builder()
                .id(3L)
                .customerId(100L)
                .type(TransactionType.REFUND)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("30.00"))
                .relatedTransactionId(2L)
                .build()

        when: "refundTransaction is called"
        ResponseEntity<TransactionResponse> result = transactionController.refundTransaction(transactionRequest)

        then: "service is called and refund is processed"
        1 * transactionService.doRefund(transactionRequest) >> expectedResponse
        result.statusCode == HttpStatus.OK
        result.body == expectedResponse
        result.body.type == TransactionType.REFUND
        result.body.status == TransactionStatus.SUCCESS
        result.body.amount == new BigDecimal("30.00")
        result.body.relatedTransactionId == 2L
    }

    def "topupTransaction should handle large amounts"() {
        given: "a topup request with large amount"
        def transactionRequest = TransactionRequest.builder()
                .customerId(100L)
                .amount(new BigDecimal("999999.99"))
                .relatedTransactionId(null)
                .build()

        def expectedResponse = TransactionResponse.builder()
                .id(1L)
                .customerId(100L)
                .type(TransactionType.TOP_UP)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("999999.99"))
                .relatedTransactionId(null)
                .build()

        when: "topupTransaction is called"
        ResponseEntity<TransactionResponse> result = transactionController.topupTransaction(transactionRequest)

        then: "service is called and large amount is processed"
        1 * transactionService.doTopup(transactionRequest) >> expectedResponse
        result.statusCode == HttpStatus.OK
        result.body.amount == new BigDecimal("999999.99")
    }

    def "purchaseTransaction should handle zero amount"() {
        given: "a purchase request with zero amount"
        def transactionRequest = TransactionRequest.builder()
                .customerId(100L)
                .amount(BigDecimal.ZERO)
                .relatedTransactionId(null)
                .build()

        def expectedResponse = TransactionResponse.builder()
                .id(2L)
                .customerId(100L)
                .type(TransactionType.PURCHASE)
                .status(TransactionStatus.SUCCESS)
                .amount(BigDecimal.ZERO)
                .relatedTransactionId(null)
                .build()

        when: "purchaseTransaction is called"
        ResponseEntity<TransactionResponse> result = transactionController.purchaseTransaction(transactionRequest)

        then: "service is called and zero amount is processed"
        1 * transactionService.doPurchase(transactionRequest) >> expectedResponse
        result.statusCode == HttpStatus.OK
        result.body.amount == BigDecimal.ZERO
    }

    def "refundTransaction should handle partial refund"() {
        given: "a partial refund request"
        def originalTransactionId = 5L
        def transactionRequest = TransactionRequest.builder()
                .customerId(100L)
                .amount(new BigDecimal("15.50"))
                .relatedTransactionId(originalTransactionId)
                .build()

        def expectedResponse = TransactionResponse.builder()
                .id(6L)
                .customerId(100L)
                .type(TransactionType.REFUND)
                .status(TransactionStatus.SUCCESS)
                .amount(new BigDecimal("15.50"))
                .relatedTransactionId(originalTransactionId)
                .build()

        when: "refundTransaction is called"
        ResponseEntity<TransactionResponse> result = transactionController.refundTransaction(transactionRequest)

        then: "service is called and partial refund is processed"
        1 * transactionService.doRefund(transactionRequest) >> expectedResponse
        result.statusCode == HttpStatus.OK
        result.body.amount == new BigDecimal("15.50")
        result.body.relatedTransactionId == originalTransactionId
    }
}
