package com.arteisis.service;

import com.arteisis.model.dto.CustomerRequest;
import com.arteisis.model.dto.CustomerResponse;
import com.arteisis.model.entity.Customer;
import com.arteisis.model.entity.Role;
import com.arteisis.repository.AppUserRepository;
import com.arteisis.repository.CustomerRepository;
import com.arteisis.repository.ShopOrderRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> list(String q) {
        List<Customer> list = (q == null || q.isBlank())
                ? customerRepository.findAllExcludingAdminUsers(Role.ADMIN)
                : customerRepository.searchExcludingAdminUsers(q.trim(), Role.ADMIN);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        Customer c = customerRepository.findById(id).orElseThrow(() -> notFound());
        ensureVisibleCustomer(c);
        return toResponse(c);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email");
        }
        ensureNotAdminEmail(request.email());
        Customer c = new Customer();
        apply(c, request);
        return toResponse(customerRepository.save(c));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer c = customerRepository.findById(id).orElseThrow(() -> notFound());
        ensureVisibleCustomer(c);
        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email");
        }
        ensureNotAdminEmail(request.email());
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

    private void ensureVisibleCustomer(Customer c) {
        if (isAdminEmail(c.getEmail())) {
            throw notFound();
        }
    }

    private void ensureNotAdminEmail(String email) {
        if (isAdminEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Este e-mail pertence a um administrador e não pode ser cadastrado como cliente.");
        }
    }

    private boolean isAdminEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return appUserRepository
                .findByEmailIgnoreCase(email.trim())
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
