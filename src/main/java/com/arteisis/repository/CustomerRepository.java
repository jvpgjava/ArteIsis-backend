package com.arteisis.repository;

import com.arteisis.model.entity.Customer;
import com.arteisis.model.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByEmailIgnoreCase(String email);

    @Query(
            """
            select c from Customer c
            where lower(c.email) not in (
                select lower(u.email) from AppUser u where u.role = :adminRole
            )
            order by c.name
            """
    )
    List<Customer> findAllExcludingAdminUsers(@Param("adminRole") Role adminRole);

    @Query(
            """
            select c from Customer c
            where lower(c.email) not in (
                select lower(u.email) from AppUser u where u.role = :adminRole
            )
              and (
                lower(c.name) like lower(concat('%', :q, '%'))
                or lower(c.email) like lower(concat('%', :q, '%'))
                or lower(c.phone) like lower(concat('%', :q, '%'))
              )
            order by c.name
            """
    )
    List<Customer> searchExcludingAdminUsers(@Param("q") String q, @Param("adminRole") Role adminRole);

    @Query(
            """
            select c from Customer c
            where lower(c.name) like lower(concat('%', :q, '%'))
               or lower(c.email) like lower(concat('%', :q, '%'))
               or lower(c.phone) like lower(concat('%', :q, '%'))
            order by c.name
            """
    )
    List<Customer> search(@Param("q") String q);
}
