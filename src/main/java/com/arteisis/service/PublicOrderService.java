package com.arteisis.service;

import com.arteisis.model.dto.OrderResponse;
import com.arteisis.model.dto.OrderWriteRequest;
import com.arteisis.model.dto.PublicOrderRequest;
import com.arteisis.model.entity.Customer;
import com.arteisis.repository.CustomerRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicOrderService {

    private final CustomerRepository customerRepository;
    private final ShopOrderService shopOrderService;

    @Transactional
    public OrderResponse createOrder(PublicOrderRequest request) {
        Customer customer = customerRepository
                .findByEmailIgnoreCase(request.customerEmail())
                .map(c -> {
                    c.setName(request.customerName());
                    c.setPhone(request.customerPhone());
                    return customerRepository.save(c);
                })
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setName(request.customerName());
                    c.setEmail(request.customerEmail().toLowerCase());
                    c.setPhone(request.customerPhone());
                    return customerRepository.save(c);
                });

        OrderWriteRequest orderRequest = new OrderWriteRequest(
                customer.getId(),
                null,
                LocalDate.now(),
                request.lines());

        return shopOrderService.create(orderRequest);
    }
}
