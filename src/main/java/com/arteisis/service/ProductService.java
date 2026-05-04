package com.arteisis.service;

import com.arteisis.model.dto.CatalogProductResponse;
import com.arteisis.model.dto.ColorVariantDto;
import com.arteisis.model.dto.ProductAdminResponse;
import com.arteisis.model.dto.ProductRequest;
import com.arteisis.model.entity.AvailabilityType;
import com.arteisis.model.entity.Product;
import com.arteisis.model.entity.ProductColorVariant;
import com.arteisis.model.entity.ProductLabel;
import com.arteisis.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
    private static final Set<String> GARMENT_CATEGORIES =
            Set.of("Camisetas", "Moletons", "Uniformes", "Infantil");
    private static final List<String> BR_SIZES_ORDER = List.of("PP", "P", "M", "G", "GG", "XGG");
    private static final Set<String> BR_SIZES_SET = Set.copyOf(BR_SIZES_ORDER);

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
        validateProduct(r);
        p.setName(r.name().trim());
        p.setUnitPrice(r.unitPrice().setScale(2, RoundingMode.HALF_UP));
        p.setCategory(r.category().trim());
        p.setStock(r.stock());
        p.setImageUrl(r.imageUrl() == null || r.imageUrl().isBlank() ? null : r.imageUrl().trim());
        p.setLabel(ProductLabel.fromApi(r.label()));
        p.setAvailability(AvailabilityType.fromApi(r.availability()));
        p.setActive(Boolean.TRUE.equals(r.active()));

        boolean garment = isGarmentCategory(r.category());
        p.getSizes().clear();
        p.getAvailableSizes().clear();
        if (garment) {
            Set<String> offered = new HashSet<>(r.sizes());
            offered.retainAll(BR_SIZES_SET);
            if (offered.isEmpty()) {
                offered = new HashSet<>(BR_SIZES_SET);
            }
            p.getSizes().addAll(offered);
            Set<String> avail = new HashSet<>(r.availableSizes());
            avail.retainAll(offered);
            p.getAvailableSizes().addAll(avail);
        }

        if (p.getColorVariants() == null) {
            p.setColorVariants(new ArrayList<>());
        } else {
            p.getColorVariants().clear();
        }
        for (ColorVariantDto dto : r.colorVariants()) {
            ProductColorVariant v = new ProductColorVariant();
            v.setHex(dto.hex().trim());
            v.setImageUrl(dto.imageUrl() == null || dto.imageUrl().isBlank() ? null : dto.imageUrl().trim());
            v.setAvailable(dto.available());
            p.getColorVariants().add(v);
        }
    }

    private void validateProduct(ProductRequest r) {
        BigDecimal price = r.unitPrice() == null ? BigDecimal.ZERO : r.unitPrice();
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O valor unitário precisa ser maior que zero.");
        }
        int stock = r.stock() == null ? 0 : r.stock();
        if (stock < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O estoque precisa ser de pelo menos 1 unidade (estoque zero não é permitido).");
        }

        boolean garment = isGarmentCategory(r.category());
        List<ColorVariantDto> colors = r.colorVariants() == null ? List.of() : r.colorVariants();
        int colorRows = colors.size();
        long availColors = colors.stream().filter(ColorVariantDto::available).count();
        int sizeCount = r.availableSizes() == null ? 0 : r.availableSizes().size();

        if (stock > 0) {
            if (garment) {
                if (sizeCount < 1) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Para esta categoria, escolha pelo menos um tamanho disponível.");
                }
                if (sizeCount > stock) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Há "
                                    + sizeCount
                                    + " tamanhos disponíveis, mas o estoque é só "
                                    + stock
                                    + ". Cada tamanho precisa de pelo menos uma unidade — reduza os tamanhos ou aumente o estoque.");
                }
                if (colorRows == 0) {
                    // só tamanhos: já validado sizeCount ≤ stock
                } else {
                    if (availColors < 1) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Com cores definidas, marque pelo menos uma como disponível ou remova todas as cores.");
                    }
                    if (availColors > stock) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Há "
                                        + availColors
                                        + " cores disponíveis, mas o estoque é só "
                                        + stock
                                        + ".");
                    }
                    long slots = (long) sizeCount * availColors;
                    if (slots > stock) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                sizeCount
                                        + " tamanhos × "
                                        + availColors
                                        + " cores = "
                                        + slots
                                        + " combinações, acima do estoque ("
                                        + stock
                                        + "). Reduza tamanhos ou cores disponíveis, ou aumente o estoque.");
                    }
                    if (stock % slots != 0) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "O estoque ("
                                        + stock
                                        + ") precisa ser múltiplo de (tamanhos × cores) = "
                                        + slots
                                        + ", para repartir igualmente cada combinação.");
                    }
                }
            } else {
                if (colorRows == 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Para esta categoria, adicione cores e marque tantas como disponíveis quanto o estoque.");
                }
                if (availColors != stock) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "O estoque é "
                                    + stock
                                    + " unidade(s): marque exatamente "
                                    + stock
                                    + " cor(es) como disponível(is), ou ajuste o estoque.");
                }
            }
        }

        if (garment) {
            for (String s : r.availableSizes()) {
                if (!BR_SIZES_SET.contains(s)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Tamanho inválido: use PP, P, M, G, GG ou XGG.");
                }
            }
        }
    }

    private static boolean isGarmentCategory(String category) {
        return category != null && GARMENT_CATEGORIES.contains(category.trim());
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
                Set.copyOf(p.getSizes()),
                Set.copyOf(p.getAvailableSizes()),
                mapColorDtos(p.getColorVariants() == null ? List.of() : p.getColorVariants()));
    }

    private CatalogProductResponse toCatalog(Product p) {
        Set<String> offered = Set.copyOf(p.getSizes());
        Set<String> avail = p.getAvailableSizes().isEmpty() && !offered.isEmpty()
                ? offered
                : Set.copyOf(p.getAvailableSizes());
        return new CatalogProductResponse(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getUnitPrice(),
                p.getImageUrl(),
                p.getLabel().toApi(),
                p.getAvailability().toApi(),
                offered,
                avail,
                mapColorDtos(p.getColorVariants() == null ? List.of() : p.getColorVariants()));
    }

    private static List<ColorVariantDto> mapColorDtos(List<ProductColorVariant> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<ColorVariantDto> out = new ArrayList<>();
        for (ProductColorVariant cv : list) {
            out.add(new ColorVariantDto(
                    cv.getHex(),
                    cv.getImageUrl() == null ? "" : cv.getImageUrl(),
                    cv.isAvailable()));
        }
        return out;
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
