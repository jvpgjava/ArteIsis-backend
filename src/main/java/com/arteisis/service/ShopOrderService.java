package com.arteisis.service;

import com.arteisis.model.dto.OrderLineRequest;
import com.arteisis.model.dto.OrderResponse;
import com.arteisis.model.dto.OrderWriteRequest;
import com.arteisis.model.entity.Customer;
import com.arteisis.model.entity.OrderLine;
import com.arteisis.model.entity.OrderStatus;
import com.arteisis.model.entity.Product;
import com.arteisis.model.entity.ShopOrder;
import com.arteisis.repository.CustomerRepository;
import com.arteisis.repository.ProductRepository;
import com.arteisis.repository.ShopOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ShopOrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> list(String q, String status, LocalDate date) {
        boolean useStatus = status != null && !status.isBlank();
        OrderStatus st = useStatus ? parseStatus(status) : OrderStatus.PENDENTE;
        boolean useDate = date != null;
        LocalDate d = useDate ? date : LocalDate.of(1970, 1, 1);
        boolean useQ = q != null && !q.isBlank();
        String qq = useQ ? q.trim() : "";
        return shopOrderRepository.findAllForAdmin(useStatus, st, useDate, d, useQ, qq).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        ShopOrder o = shopOrderRepository.loadDetails(id).orElseThrow(() -> notFound());
        return toResponse(o);
    }

    @Transactional
    public OrderResponse create(OrderWriteRequest request) {
        Customer c = customerRepository.findById(request.customerId()).orElseThrow(() -> notFound());
        ShopOrder o = new ShopOrder();
        o.setCustomer(c);
        o.setStatus(parseStatus(request.status()));
        o.setOrderDate(request.orderDate());
        applyLines(o, request.lines());
        o.setTotalAmount(sumLines(o));
        return toResponse(shopOrderRepository.save(o));
    }

    @Transactional
    public OrderResponse update(UUID id, OrderWriteRequest request) {
        ShopOrder o = shopOrderRepository.findById(id).orElseThrow(() -> notFound());
        Customer c = customerRepository.findById(request.customerId()).orElseThrow(() -> notFound());
        o.setCustomer(c);
        o.setStatus(parseStatus(request.status()));
        o.setOrderDate(request.orderDate());
        o.clearLines();
        applyLines(o, request.lines());
        o.setTotalAmount(sumLines(o));
        return toResponse(shopOrderRepository.save(o));
    }

    @Transactional
    public void delete(UUID id) {
        if (!shopOrderRepository.existsById(id)) {
            throw notFound();
        }
        shopOrderRepository.deleteById(id);
    }

    private void applyLines(ShopOrder o, List<OrderLineRequest> lines) {
        for (OrderLineRequest lr : lines) {
            OrderLine line = new OrderLine();
            line.setDescription(lr.description().trim());
            line.setQuantity(lr.quantity());
            BigDecimal up = lr.unitPrice().setScale(2, RoundingMode.HALF_UP);
            line.setUnitPrice(up);
            line.setLineTotal(up.multiply(BigDecimal.valueOf(lr.quantity())).setScale(2, RoundingMode.HALF_UP));
            if (lr.productId() != null) {
                Product p = productRepository.findById(lr.productId()).orElseThrow(() -> notFound());
                line.setProduct(p);
            }
            o.addLine(line);
        }
    }

    private BigDecimal sumLines(ShopOrder o) {
        return o.getLines().stream()
                .map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private OrderResponse toResponse(ShopOrder o) {
        String summary = o.getLines().stream()
                .map(OrderLine::getDescription)
                .collect(Collectors.joining(", "));
        return new OrderResponse(
                o.getId(),
                o.getCustomer().getId(),
                o.getCustomer().getName(),
                summary,
                o.getOrderDate(),
                o.getStatus().toApi(),
                o.getTotalAmount());
    }

    private static OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return OrderStatus.PENDENTE;
        }
        try {
            return OrderStatus.fromApi(status);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status");
        }
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
