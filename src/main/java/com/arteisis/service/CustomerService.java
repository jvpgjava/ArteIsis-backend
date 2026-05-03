package com.arteisis.service;

import com.arteisis.model.dto.CustomerRequest;
import com.arteisis.model.dto.CustomerResponse;
import com.arteisis.model.entity.Customer;
import com.arteisis.repository.CustomerRepository;
import com.arteisis.repository.ShopOrderRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ShopOrderRepository shopOrderRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> list(String q) {
        List<Customer> list = (q == null || q.isBlank())
                ? customerRepository.findAll(Sort.by("name"))
                : customerRepository.search(q.trim());
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        return toResponse(customerRepository.findById(id).orElseThrow(() -> notFound()));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email");
        }
        Customer c = new Customer();
        apply(c, request);
        return toResponse(customerRepository.save(c));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer c = customerRepository.findById(id).orElseThrow(() -> notFound());
        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email");
        }
        apply(c, request);
        return toResponse(customerRepository.save(c));
    }

    @Transactional
    public void delete(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw notFound();
        }
        if (shopOrderRepository.existsByCustomer_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "orders");
        }
        customerRepository.deleteById(id);
    }

    private void apply(Customer c, CustomerRequest r) {
        c.setName(r.name().trim());
        c.setEmail(r.email().trim().toLowerCase());
        c.setPhone(r.phone().trim());
    }

    private CustomerResponse toResponse(Customer c) {
        long n = shopOrderRepository.countByCustomer_Id(c.getId());
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), n);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
