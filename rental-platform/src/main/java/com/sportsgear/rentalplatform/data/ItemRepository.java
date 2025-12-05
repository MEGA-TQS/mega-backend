package com.sportsgear.rentalplatform.data;

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
            + "(i.active = true)")
    List<Item> searchItems(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("location") String location
    );
}
