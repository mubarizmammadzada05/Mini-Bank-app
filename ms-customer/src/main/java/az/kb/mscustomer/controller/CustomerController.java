package az.kb.mscustomer.controller;


import az.kb.mscustomer.entity.Customer;
import az.kb.mscustomer.model.CreateCustomerRequest;
import az.kb.mscustomer.model.UpdateBalanceRequest;
import az.kb.mscustomer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody CreateCustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<Customer> updateBalance(@PathVariable Long id, @RequestBody UpdateBalanceRequest request) {
        return ResponseEntity.ok(customerService.updateBalance(id, request.getAmount()));
    }

}

