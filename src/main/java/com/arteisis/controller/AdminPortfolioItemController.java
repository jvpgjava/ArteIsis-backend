package com.arteisis.controller;

import com.arteisis.model.dto.PortfolioItemAdminResponse;
import com.arteisis.model.dto.PortfolioItemRequest;
import com.arteisis.service.PortfolioItemService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/portfolio-items")
@Validated
@RequiredArgsConstructor
public class AdminPortfolioItemController {

    private final PortfolioItemService portfolioItemService;

    @GetMapping
    public List<PortfolioItemAdminResponse> list() {
        return portfolioItemService.listAdmin();
    }

    @GetMapping("/{id}")
    public PortfolioItemAdminResponse get(@PathVariable UUID id) {
        return portfolioItemService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioItemAdminResponse create(@Valid @RequestBody PortfolioItemRequest request) {
        return portfolioItemService.create(request);
    }

    @PutMapping("/{id}")
    public PortfolioItemAdminResponse update(@PathVariable UUID id, @Valid @RequestBody PortfolioItemRequest request) {
        return portfolioItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        portfolioItemService.delete(id);
    }
}
