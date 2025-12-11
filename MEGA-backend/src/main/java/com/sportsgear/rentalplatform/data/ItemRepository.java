package com.sportsgear.rentalplatform.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE "
            + "(:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:category IS NULL OR i.category = :category) AND "
            + "(:location IS NULL OR i.location = :location) AND "
            + "(:minPrice IS NULL OR i.pricePerDay >= :minPrice) AND "
            + "(:maxPrice IS NULL OR i.pricePerDay <= :maxPrice) AND "
            + "(i.active = true) AND "
            // Lógica de Disponibilidade
            + "((:startDate IS NULL OR :endDate IS NULL) OR i.id NOT IN ("
            + "    SELECT bi.item.id FROM Booking b JOIN b.items bi WHERE "
            + "    b.status IN ('PENDING', 'APPROVED', 'PAID', 'BLOCKED') AND " 
            + "    (b.endDate > :startDate AND b.startDate < :endDate)"
            + "))"
            )
    List<Item> searchItems(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    List<Item> findByOwnerId(Long ownerId);
}