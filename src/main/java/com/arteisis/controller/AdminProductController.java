package com.arteisis.controller;

import com.arteisis.model.dto.ProductAdminResponse;
import com.arteisis.model.dto.ProductRequest;
import com.arteisis.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@Validated
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductAdminResponse> list(@RequestParam(name = "q", required = false) String q) {
        return productService.listAdmin(q);
    }

    @GetMapping("/{id}")
    public ProductAdminResponse get(@PathVariable UUID id) {
        return productService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductAdminResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductAdminResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}
