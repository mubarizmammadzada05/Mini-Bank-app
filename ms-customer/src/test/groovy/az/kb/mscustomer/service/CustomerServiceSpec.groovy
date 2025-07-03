package az.kb.mscustomer.service

import az.kb.mscustomer.entity.Customer
import az.kb.mscustomer.exception.InsufficientBalanceException
import az.kb.mscustomer.exception.NotFoundException
import az.kb.mscustomer.model.CreateCustomerRequest
import az.kb.mscustomer.repository.CustomerRepository
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CustomerServiceSpec extends Specification {

    CustomerRepository repository = Mock()
    CustomerService customerService = new CustomerService(repository)

    def "createCustomer should save customer with initial balance 100 and return saved customer"() {
        given:
        def request = new CreateCustomerRequest(
                name: "John",
                surname: "Doe",
                birthDate: LocalDate.parse("01.01.1990", DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                phoneNumber: "+123456789"
        )

        def savedCustomer = Customer.builder()
                .id(1L)
                .name("John")
                .surname("Doe")
                .birthDate(request.birthDate)
                .phoneNumber("+123456789")
                .balance(BigDecimal.valueOf(100))
                .build()

        when:
        Customer result = customerService.createCustomer(request)

        then:
        1 * repository.save(_ as Customer) >> { Customer customer ->
            assert customer.name == "John"
            assert customer.surname == "Doe"
            assert customer.birthDate == request.birthDate
            assert customer.phoneNumber == "+123456789"
            assert customer.balance == BigDecimal.valueOf(100)
            return savedCustomer
        }
        result == savedCustomer
    }

    def "getCustomer should return customer when found"() {
        given:
        Long customerId = 1L
        def customer = Customer.builder()
                .id(customerId)
                .name("Jane")
                .surname("Smith")
                .birthDate(LocalDate.parse("15.05.1985", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+987654321")
                .balance(BigDecimal.valueOf(200))
                .build()

        when:
        Customer result = customerService.getCustomer(customerId)

        then:
        1 * repository.findById(customerId) >> Optional.of(customer)
        result == customer
    }

    def "getCustomer should throw NotFoundException when customer not found"() {
        given:
        Long customerId = 999L

        when:
        customerService.getCustomer(customerId)

        then:
        1 * repository.findById(customerId) >> Optional.empty()
        NotFoundException exception = thrown()
        exception.message.contains("not found")
    }

    def "updateBalance should add positive amount to existing balance and save customer"() {
        given:
        Long customerId = 1L
        BigDecimal amount = BigDecimal.valueOf(50)

        def existingCustomer = Customer.builder()
                .id(customerId)
                .name("Alice")
                .surname("Johnson")
                .birthDate(LocalDate.parse("20.10.1992", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+111222333")
                .balance(BigDecimal.valueOf(100))
                .build()

        def updatedCustomer = Customer.builder()
                .id(customerId)
                .name("Alice")
                .surname("Johnson")
                .birthDate(LocalDate.parse("20.10.1992", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+111222333")
                .balance(BigDecimal.valueOf(150))
                .build()

        when:
        Customer result = customerService.updateBalance(customerId, amount)

        then:
        1 * repository.findById(customerId) >> Optional.of(existingCustomer)
        1 * repository.save(_ as Customer) >> { Customer customer ->
            assert customer.balance == BigDecimal.valueOf(150)
            return updatedCustomer
        }
        result == updatedCustomer
    }

    def "updateBalance should subtract negative amount from existing balance and save customer"() {
        given:
        Long customerId = 1L
        BigDecimal amount = BigDecimal.valueOf(-30)

        def existingCustomer = Customer.builder()
                .id(customerId)
                .name("Bob")
                .surname("Wilson")
                .birthDate(LocalDate.parse("10.03.1988", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+444555666")
                .balance(BigDecimal.valueOf(100))
                .build()

        def updatedCustomer = Customer.builder()
                .id(customerId)
                .name("Bob")
                .surname("Wilson")
                .birthDate(LocalDate.parse("10.03.1988", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+444555666")
                .balance(BigDecimal.valueOf(70))
                .build()

        when:
        Customer result = customerService.updateBalance(customerId, amount)

        then:
        1 * repository.findById(customerId) >> Optional.of(existingCustomer)
        1 * repository.save(_ as Customer) >> { Customer customer ->
            assert customer.balance == BigDecimal.valueOf(70)
            return updatedCustomer
        }
        result == updatedCustomer
    }

    def "updateBalance should throw InsufficientBalanceException when new balance would be negative"() {
        given:
        Long customerId = 1L
        BigDecimal amount = BigDecimal.valueOf(-150)

        def existingCustomer = Customer.builder()
                .id(customerId)
                .name("Charlie")
                .surname("Brown")
                .birthDate(LocalDate.parse("05.07.1995", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+777888999")
                .balance(BigDecimal.valueOf(100))
                .build()

        when:
        customerService.updateBalance(customerId, amount)

        then:
        1 * repository.findById(customerId) >> Optional.of(existingCustomer)
        0 * repository.save(_ as Customer)
        InsufficientBalanceException exception = thrown()
        exception.message.contains("Insufficient")
    }

    def "updateBalance should throw NotFoundException when customer does not exist"() {
        given:
        Long customerId = 999L
        BigDecimal amount = BigDecimal.valueOf(50)

        when:
        customerService.updateBalance(customerId, amount)

        then:
        1 * repository.findById(customerId) >> Optional.empty()
        0 * repository.save(_ as Customer)
        NotFoundException exception = thrown()
        exception.message.contains("not found")
    }

    def "updateBalance should allow balance to become exactly zero"() {
        given:
        Long customerId = 1L
        BigDecimal amount = BigDecimal.valueOf(-100)

        def existingCustomer = Customer.builder()
                .id(customerId)
                .name("David")
                .surname("Miller")
                .birthDate(LocalDate.parse("12.12.1990", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+000111222")
                .balance(BigDecimal.valueOf(100))
                .build()

        def updatedCustomer = Customer.builder()
                .id(customerId)
                .name("David")
                .surname("Miller")
                .birthDate(LocalDate.parse("12.12.1990", DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .phoneNumber("+000111222")
                .balance(BigDecimal.ZERO)
                .build()

        when:
        Customer result = customerService.updateBalance(customerId, amount)

        then:
        1 * repository.findById(customerId) >> Optional.of(existingCustomer)
        1 * repository.save(_ as Customer) >> { Customer customer ->
            assert customer.balance == BigDecimal.ZERO
            return updatedCustomer
        }
        result == updatedCustomer
    }
}