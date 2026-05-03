package com.arteisis.service;

import com.arteisis.model.dto.CatalogProductResponse;
import com.arteisis.model.dto.ProductAdminResponse;
import com.arteisis.model.dto.ProductRequest;
import com.arteisis.model.entity.AvailabilityType;
import com.arteisis.model.entity.Product;
import com.arteisis.model.entity.ProductLabel;
import com.arteisis.repository.ProductRepository;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final List<String> DUMMY_IN = List.of("__arteisis_no_filter__");

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductAdminResponse> listAdmin(String q) {
        boolean qBlank = q == null || q.isBlank();
        String qq = qBlank ? "" : q.trim();
        return productRepository.findAllForAdmin(qBlank, qq).stream().map(this::toAdmin).toList();
    }

    @Transactional(readOnly = true)
    public ProductAdminResponse get(UUID id) {
        return toAdmin(productRepository.findById(id).orElseThrow(() -> notFound()));
    }

    @Transactional(readOnly = true)
    public CatalogProductResponse getCatalog(UUID id) {
        Product p = productRepository.findById(id).orElseThrow(() -> notFound());
        if (!p.isActive()) {
            throw notFound();
        }
        return toCatalog(p);
    }

    @Transactional(readOnly = true)
    public List<CatalogProductResponse> listCatalog(
            String q, Set<String> categories, Set<String> sizes, AvailabilityType availability) {
        boolean useAvailability = availability != null;
        boolean useQ = q != null && !q.isBlank();
        String qq = useQ ? q.trim() : "";
        boolean useCategories = categories != null && !categories.isEmpty();
        boolean useSizes = sizes != null && !sizes.isEmpty();
        List<String> catList = useCategories ? List.copyOf(categories) : DUMMY_IN;
        List<String> sizeList = useSizes ? List.copyOf(sizes) : DUMMY_IN;
        AvailabilityType av = useAvailability ? availability : AvailabilityType.DISPONIVEL;
        return productRepository
                .findCatalog(useAvailability, av, useQ, qq, useCategories, catList, useSizes, sizeList)
                .stream()
                .map(this::toCatalog)
                .toList();
    }

    @Transactional
    public ProductAdminResponse create(ProductRequest request) {
        Product p = new Product();
        apply(p, request);
        return toAdmin(productRepository.save(p));
    }

    @Transactional
    public ProductAdminResponse update(UUID id, ProductRequest request) {
        Product p = productRepository.findById(id).orElseThrow(() -> notFound());
        apply(p, request);
        return toAdmin(productRepository.save(p));
    }

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw notFound();
        }
        productRepository.deleteById(id);
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim());
        p.setUnitPrice(r.unitPrice().setScale(2, RoundingMode.HALF_UP));
        p.setCategory(r.category().trim());
        p.setStock(r.stock());
        p.setImageUrl(r.imageUrl() == null || r.imageUrl().isBlank() ? null : r.imageUrl().trim());
        p.setLabel(ProductLabel.fromApi(r.label()));
        p.setAvailability(AvailabilityType.fromApi(r.availability()));
        p.setActive(Boolean.TRUE.equals(r.active()));
        p.setSizes(r.sizes() == null ? new HashSet<>() : new HashSet<>(r.sizes()));
    }

    private ProductAdminResponse toAdmin(Product p) {
        return new ProductAdminResponse(
                p.getId(),
                p.getName(),
                p.getUnitPrice(),
                p.getCategory(),
                p.getStock(),
                p.getImageUrl(),
                p.getLabel().toApi(),
                p.getAvailability().toApi(),
                p.isActive(),
                Set.copyOf(p.getSizes()));
    }

    private CatalogProductResponse toCatalog(Product p) {
        return new CatalogProductResponse(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getUnitPrice(),
                p.getImageUrl(),
                p.getLabel().toApi(),
                p.getAvailability().toApi(),
                Set.copyOf(p.getSizes()));
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
