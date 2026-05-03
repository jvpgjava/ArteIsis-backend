package com.arteisis.repository;

import com.arteisis.model.entity.OrderStatus;
import com.arteisis.model.entity.ShopOrder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, UUID> {

    long countByCustomer_Id(UUID customerId);

    boolean existsByCustomer_Id(UUID customerId);

    @Query(
            """
            select distinct o from ShopOrder o
            join o.customer c
            left join o.lines l
            where (:useStatus = false or o.status = :status)
              and (:useDate = false or o.orderDate = :orderDate)
              and (:useQ = false
                   or (lower(c.name) like lower(concat('%', :q, '%')))
                   or (lower(l.description) like lower(concat('%', :q, '%'))))
            order by o.orderDate desc
            """)
    List<ShopOrder> findAllForAdmin(
            @Param("useStatus") boolean useStatus,
            @Param("status") OrderStatus status,
            @Param("useDate") boolean useDate,
            @Param("orderDate") LocalDate orderDate,
            @Param("useQ") boolean useQ,
            @Param("q") String q);

    @Query(
            """
            select distinct o from ShopOrder o
            join fetch o.customer
            join fetch o.lines l
            left join fetch l.product
            where o.id = :id
            """)
    Optional<ShopOrder> loadDetails(@Param("id") UUID id);
}
