package com.arteisis.controller;

import com.arteisis.model.dto.OrderResponse;
import com.arteisis.model.dto.OrderWriteRequest;
import com.arteisis.service.ShopOrderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@Validated
@RequiredArgsConstructor
public class AdminOrderController {

    private final ShopOrderService shopOrderService;

    @GetMapping
    public List<OrderResponse> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        return shopOrderService.list(q, status, date);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return shopOrderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderWriteRequest request) {
        return shopOrderService.create(request);
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable UUID id, @Valid @RequestBody OrderWriteRequest request) {
        return shopOrderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        shopOrderService.delete(id);
    }
}
