package com.arteisis.repository;

import com.arteisis.model.entity.PortfolioItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, UUID> {

    List<PortfolioItem> findByActiveTrueOrderBySortOrderAscTitleAsc();

    List<PortfolioItem> findAllByOrderBySortOrderAscTitleAsc();

    @Query("select coalesce(max(p.sortOrder), -1) from PortfolioItem p")
    int findMaxSortOrder();
}
