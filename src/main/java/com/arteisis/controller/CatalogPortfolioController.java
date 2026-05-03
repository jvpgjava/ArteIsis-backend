package com.arteisis.controller;

import com.arteisis.model.dto.PortfolioItemResponse;
import com.arteisis.service.PortfolioItemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/portfolio")
@RequiredArgsConstructor
public class CatalogPortfolioController {

    private final PortfolioItemService portfolioItemService;

    @GetMapping
    public List<PortfolioItemResponse> list() {
        return portfolioItemService.listPublic();
    }
}
