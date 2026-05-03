package com.arteisis.controller;

import com.arteisis.model.dto.CatalogProductResponse;
import com.arteisis.model.entity.AvailabilityType;
import com.arteisis.service.ProductService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public CatalogProductResponse get(@PathVariable UUID id) {
        return productService.getCatalog(id);
    }

    @GetMapping
    public List<CatalogProductResponse> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "category", required = false) List<String> category,
            @RequestParam(name = "size", required = false) List<String> size,
            @RequestParam(name = "availability", required = false) String availability) {
        AvailabilityType av = null;
        if (availability != null && !availability.isBlank()) {
            String a = availability.trim();
            if (!a.equalsIgnoreCase("all")) {
                av = AvailabilityType.fromApi(a);
            }
        }
        Set<String> cats = category == null || category.isEmpty() ? Set.of() : new HashSet<>(category);
        Set<String> sizes = size == null || size.isEmpty() ? Set.of() : new HashSet<>(size);
        return productService.listCatalog(q, cats, sizes, av);
    }
}
