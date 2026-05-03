package com.arteisis.service;

import com.arteisis.model.dto.PortfolioItemAdminResponse;
import com.arteisis.model.dto.PortfolioItemRequest;
import com.arteisis.model.dto.PortfolioItemResponse;
import com.arteisis.model.entity.PortfolioItem;
import com.arteisis.repository.PortfolioItemRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PortfolioItemService {

    private final PortfolioItemRepository repository;

    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> listPublic() {
        return repository.findByActiveTrueOrderBySortOrderAscTitleAsc().stream()
                .map(this::toPublic)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioItemAdminResponse> listAdmin() {
        return repository.findAllByOrderBySortOrderAscTitleAsc().stream()
                .map(this::toAdmin)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioItemAdminResponse get(UUID id) {
        return toAdmin(findOr404(id));
    }

    @Transactional
    public PortfolioItemAdminResponse create(PortfolioItemRequest request) {
        PortfolioItem e = new PortfolioItem();
        e.setSortOrder(repository.findMaxSortOrder() + 1);
        apply(e, request);
        return toAdmin(repository.save(e));
    }

    @Transactional
    public PortfolioItemAdminResponse update(UUID id, PortfolioItemRequest request) {
        PortfolioItem e = findOr404(id);
        apply(e, request);
        return toAdmin(repository.save(e));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
    }

    private PortfolioItem findOr404(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(PortfolioItem e, PortfolioItemRequest request) {
        e.setTitle(request.title().trim());
        e.setImageUrl(request.imageUrl().trim());
        e.setActive(request.active());
    }

    private PortfolioItemResponse toPublic(PortfolioItem e) {
        return new PortfolioItemResponse(e.getId(), e.getTitle(), e.getImageUrl(), e.getSortOrder());
    }

    private PortfolioItemAdminResponse toAdmin(PortfolioItem e) {
        return new PortfolioItemAdminResponse(
                e.getId(), e.getTitle(), e.getImageUrl(), e.getSortOrder(), e.isActive());
    }
}
