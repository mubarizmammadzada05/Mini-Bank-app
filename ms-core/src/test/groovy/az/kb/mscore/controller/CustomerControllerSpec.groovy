package az.kb.mscore.controller
import az.kb.mscore.model.CustomerResponseDto
import az.kb.mscore.model.CustomerCreateRequestDto
import az.kb.mscore.service.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import java.time.LocalDate
import java.time.LocalDateTime


class CustomerControllerSpec extends Specification {

    CustomerService customerService = Mock()
    CustomerController customerController = new CustomerController(customerService)

    def "getCustomer should return customer when found"() {
        given: "a customer exists"
        def customerId = 1L
        def customerResponse = CustomerResponseDto.builder()
                .id(customerId)
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.of(2000, 1, 26))
                .balance(BigDecimal.valueOf(1000.00))
                .phoneNumber("+994501234567")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()

        when: "getCustomer is called"
        ResponseEntity<CustomerResponseDto> result = customerController.getCustomer(customerId)

        then: "service is called and customer is returned"
        1 * customerService.getCustomerById(customerId) >> customerResponse
        result.statusCode == HttpStatus.OK
        result.body == customerResponse
        result.body.id == customerId
        result.body.name == "Mubariz"
        result.body.surname == "Mammadzade"
        result.body.birthDate == LocalDate.of(2000, 1, 26)
        result.body.balance == BigDecimal.valueOf(1000.00)
        result.body.phoneNumber == "+994501234567"
    }

    def "createCustomer should return created customer"() {
        given: "a valid customer create request"
        def customerCreateRequest = CustomerCreateRequestDto.builder()
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.of(2000, 1, 26))
                .phoneNumber("+994501234567")
                .build()

        def customerResponse = CustomerResponseDto.builder()
                .id(1L)
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.of(2000, 1, 26))
                .balance(BigDecimal.ZERO)
                .phoneNumber("+994501234567")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()

        when: "createCustomer is called"
        ResponseEntity<CustomerResponseDto> result = customerController.createCustomer(customerCreateRequest)

        then: "service is called and customer is created"
        1 * customerService.createCustomer(customerCreateRequest) >> customerResponse
        result.statusCode == HttpStatus.OK
        result.body == customerResponse
        result.body.id == 1L
        result.body.name == "Mubariz"
        result.body.surname == "Mammadzade"
        result.body.birthDate == LocalDate.of(2000, 1, 26)
        result.body.balance == BigDecimal.ZERO
        result.body.phoneNumber == "+994501234567"
    }

    def "getCustomer should throw exception when customer not found"() {
        given: "customer does not exist"
        def customerId = 99L

        when: "getCustomer is called"
        customerController.getCustomer(customerId)

        then: "service throws exception"
        1 * customerService.getCustomerById(customerId) >> { throw new RuntimeException("Customer not found") }
        thrown(RuntimeException)
    }

    def "createCustomer should handle customer with minimum required fields"() {
        given: "a minimal customer create request"
        def customerCreateRequest = CustomerCreateRequestDto.builder()
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.of(2000, 1, 26))
                .phoneNumber("+994501234567")
                .build()

        def customerResponse = CustomerResponseDto.builder()
                .id(2L)
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.of(2000, 1, 26))
                .balance(BigDecimal.ZERO)
                .phoneNumber("+994501234567")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()

        when: "createCustomer is called"
        ResponseEntity<CustomerResponseDto> result = customerController.createCustomer(customerCreateRequest)

        then: "service is called and customer is created with default values"
        1 * customerService.createCustomer(customerCreateRequest) >> customerResponse
        result.statusCode == HttpStatus.OK
        result.body.name == "Mubariz"
        result.body.surname == "Mammadzade"
        result.body.birthDate == LocalDate.of(2000, 1, 26)
        result.body.balance == BigDecimal.ZERO
        result.body.phoneNumber == "+994501234567"
        result.body.createdAt != null
        result.body.updatedAt != null
    }

    def "createCustomer should throw exception when service fails"() {
        given: "invalid customer create request"
        def customerCreateRequest = CustomerCreateRequestDto.builder()
                .name("Mubariz")
                .surname("Mammadzade")
                .birthDate(LocalDate.of(2000, 1, 26))
                .phoneNumber("invalid-phone")
                .build()

        when: "createCustomer is called"
        customerController.createCustomer(customerCreateRequest)

        then: "service throws exception"
        1 * customerService.createCustomer(customerCreateRequest) >> { throw new RuntimeException("Invalid phone number") }
        thrown(RuntimeException)
    }
}