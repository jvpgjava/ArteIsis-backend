package com.arteisis.service;

import com.arteisis.model.dto.OrderResponse;
import com.arteisis.model.dto.OrderWriteRequest;
import com.arteisis.model.dto.PublicOrderRequest;
import com.arteisis.model.entity.Customer;
import com.arteisis.repository.CustomerRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PublicOrderService {

    private final CustomerRepository customerRepository;
    private final ShopOrderService shopOrderService;
    private final NotificationMailService notificationMailService;

    @Transactional
    public OrderResponse createOrder(PublicOrderRequest request, String email) {
        Customer customer = customerRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY, "Cadastro de cliente não encontrado para este e-mail."));

        OrderWriteRequest orderRequest = new OrderWriteRequest(
                customer.getId(),
                null,
                LocalDate.now(),
                request.lines());

        OrderResponse response = shopOrderService.create(orderRequest);
        notificationMailService.sendOrderConfirmation(customer.getEmail(), customer.getName(), response);
        return response;
    }
}
