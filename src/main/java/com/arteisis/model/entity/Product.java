package com.arteisis.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private int stock;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProductLabel label = ProductLabel.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AvailabilityType availability = AvailabilityType.DISPONIVEL;

    @Column(nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size_code", nullable = false, length = 8)
    private Set<String> sizes = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (label == null) {
            label = ProductLabel.NONE;
        }
        if (availability == null) {
            availability = AvailabilityType.DISPONIVEL;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
