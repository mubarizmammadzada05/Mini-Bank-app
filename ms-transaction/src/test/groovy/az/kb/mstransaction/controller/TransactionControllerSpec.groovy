package az.kb.mstransaction.controller

import az.kb.mstransaction.entity.Transaction
import az.kb.mstransaction.model.TransactionRequest
import az.kb.mstransaction.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class TransactionControllerSpec extends Specification {

    TransactionService transactionService = Mock()
    TransactionController controller = new TransactionController(transactionService)

    def "topUp should return created transaction"() {
        given:
        def request = new TransactionRequest(customerId: 1L, amount: 100)
        def transaction = new Transaction(id: 1L, amount: 100)

        when:
        transactionService.topUp(request) >> transaction
        ResponseEntity<Transaction> response = controller.topUp(request)

        then:
        response.statusCode == HttpStatus.OK
        response.body == transaction
    }

    def "purchase should return created transaction"() {
        given:
        def request = new TransactionRequest(customerId: 2L, amount: 50)
        def transaction = new Transaction(id: 2L, amount: 50)

        when:
        transactionService.purchase(request) >> transaction
        ResponseEntity<Transaction> response = controller.purchase(request)

        then:
        response.statusCode == HttpStatus.OK
        response.body == transaction
    }

    def "refund should return created transaction"() {
        given:
        def request = new TransactionRequest(customerId: 3L, amount: 20, relatedTransactionId: 10L)
        def transaction = new Transaction(id: 3L, amount: 20)

        when:
        transactionService.refund(request) >> transaction
        ResponseEntity<Transaction> response = controller.refund(request)

        then:
        response.statusCode == HttpStatus.OK
        response.body == transaction
    }

    def "getAllTransactions should return list of transactions"() {
        given:
        def transactions = [new Transaction(id: 1L), new Transaction(id: 2L)]

        when:
        transactionService.getAllTransactions() >> transactions
        ResponseEntity<List<Transaction>> response = controller.getAllTransactions()

        then:
        response.statusCode == HttpStatus.OK
        response.body == transactions
    }

    def "getCustomerTransactions should return transaction for customer"() {
        given:
        Long customerId = 1L
        def transaction = List.of(new Transaction(id: 5L))

        when:
        transactionService.getTransactionByCustomerId(customerId) >> transaction
        ResponseEntity<List<Transaction>> response = controller.getCustomerTransactions(customerId)

        then:
        response.statusCode == HttpStatus.OK
        response.body == transaction
    }

    def "getTransactionById should return transaction by id"() {
        given:
        Long id = 1L
        def transaction = new Transaction(id: id)

        when:
        transactionService.getTransactionById(id) >> transaction
        ResponseEntity<Transaction> response = controller.getTransactionById(id)

        then:
        response.statusCode == HttpStatus.OK
        response.body == transaction
    }
}
