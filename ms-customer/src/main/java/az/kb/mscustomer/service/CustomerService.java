package az.kb.mscustomer.service;

import az.kb.mscustomer.entity.Customer;
import az.kb.mscustomer.exception.InsufficientBalanceException;
import az.kb.mscustomer.exception.NotFoundException;
import az.kb.mscustomer.model.CreateCustomerRequest;
import az.kb.mscustomer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static az.kb.mscustomer.constant.ErrorMessage.CUSTOMER_NOT_FOUND;
import static az.kb.mscustomer.constant.ErrorMessage.INSUFFICIENT_BALANCE_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .birthDate(request.getBirthDate())
                .phoneNumber(request.getPhoneNumber())
                .balance(BigDecimal.valueOf(100))
                .build();
        return repository.save(customer);
    }

    public Customer getCustomer(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(CUSTOMER_NOT_FOUND, id));
    }

    public Customer updateBalance(Long id, BigDecimal amount) {
        Customer customer = getCustomer(id);
        BigDecimal newBalance = customer.getBalance().add(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException(INSUFFICIENT_BALANCE_MESSAGE);
        }

        customer.setBalance(newBalance);
        return repository.save(customer);
    }
}