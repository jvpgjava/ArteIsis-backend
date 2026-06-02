package com.arteisis.controller;

import com.arteisis.model.dto.OrderResponse;
import com.arteisis.model.dto.PublicOrderRequest;
import com.arteisis.service.PublicOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/orders")
@RequiredArgsConstructor
public class PublicOrderController {

    private final PublicOrderService publicOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid PublicOrderRequest request) {
        return publicOrderService.createOrder(request);
    }
}
