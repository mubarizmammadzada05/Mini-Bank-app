package az.kb.mscustomer.controller

import az.kb.mscustomer.entity.Customer
import az.kb.mscustomer.model.CreateCustomerRequest
import az.kb.mscustomer.model.UpdateBalanceRequest
import az.kb.mscustomer.service.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CustomerControllerSpec extends Specification {

    CustomerService customerService = Mock()
    CustomerController customerController = new CustomerController(customerService)

    def "CreateCustomer Success"() {
        given:
        def request = new CreateCustomerRequest(
                name: "Mubariz",
                surname: "Mammadzade",
                birthDate: LocalDate.parse("26.01.2000", DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                phoneNumber: "+123456789"
        )

        def customer = Customer.builder()
                .id(1L)
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(request.birthDate)
                .phoneNumber("+123456789")
                .balance(BigDecimal.ZERO)
                .build()

        when:
        ResponseEntity<Customer> response = customerController.createCustomer(request)

        then:
        1 * customerService.createCustomer(request) >> customer
        response.statusCode == HttpStatus.OK
        response.body == customer
    }

    def "GetCustomer Success"() {
        given:
        Long customerId = 1L

        def customer = Customer.builder()
                .id(customerId)
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.parse("26.01.2000", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+987654321")
                .balance(BigDecimal.TEN)
                .build()

        when:
        ResponseEntity<Customer> response = customerController.getCustomer(customerId)

        then:
        1 * customerService.getCustomer(customerId) >> customer
        response.statusCode == HttpStatus.OK
        response.body == customer
    }

    def "UpdateBalance Success"() {
        given:
        Long customerId = 2L
        def request = new UpdateBalanceRequest(amount: BigDecimal.valueOf(150))

        def updatedCustomer = Customer.builder()
                .id(customerId)
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.parse("26.01.2000", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+111222333")
                .balance(request.amount)
                .build()

        when:
        ResponseEntity<Customer> response = customerController.updateBalance(customerId, request)

        then:
        1 * customerService.updateBalance(customerId, request.amount) >> updatedCustomer
        response.statusCode == HttpStatus.OK
        response.body == updatedCustomer
    }
}