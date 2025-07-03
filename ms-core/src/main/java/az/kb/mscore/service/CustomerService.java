package az.kb.mscore.service;

import az.kb.mscore.client.CustomerClient;
import az.kb.mscore.model.CustomerCreateRequestDto;
import az.kb.mscore.model.CustomerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerClient customerClient;


    public CustomerResponseDto getCustomerById(Long id) {
        return customerClient.getCustomer(id);
    }

    public CustomerResponseDto createCustomer(CustomerCreateRequestDto requestDto) {
        return customerClient.createCustomer(requestDto);
    }

}
