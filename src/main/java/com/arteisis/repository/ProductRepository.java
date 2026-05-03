package com.arteisis.repository;

import com.arteisis.model.entity.AvailabilityType;
import com.arteisis.model.entity.Product;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(
            """
            select p from Product p
            where (:qBlank = true
                or lower(p.name) like lower(concat('%', :q, '%'))
                or lower(p.category) like lower(concat('%', :q, '%')))
            order by p.name
            """)
    List<Product> findAllForAdmin(@Param("qBlank") boolean qBlank, @Param("q") String q);

    @Query(
            """
            select distinct p from Product p
            left join p.sizes sz
            where p.active = true
              and (:useAvailability = false or p.availability = :availability)
              and (:useQ = false
                   or (lower(p.name) like lower(concat('%', :q, '%')))
                   or (lower(p.category) like lower(concat('%', :q, '%'))))
              and (:useCategories = false or p.category in :categories)
              and (:useSizes = false or sz in :sizes)
            order by p.name
            """)
    List<Product> findCatalog(
            @Param("useAvailability") boolean useAvailability,
            @Param("availability") AvailabilityType availability,
            @Param("useQ") boolean useQ,
            @Param("q") String q,
            @Param("useCategories") boolean useCategories,
            @Param("categories") List<String> categories,
            @Param("useSizes") boolean useSizes,
            @Param("sizes") List<String> sizes);
}
