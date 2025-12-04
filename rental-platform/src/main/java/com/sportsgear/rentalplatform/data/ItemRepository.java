package com.sportsgear.rentalplatform.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // SELECT * FROM items WHERE category = ? AND available = true
    List<Item> findByCategoryAndAvailableTrue(String category);
    
    // Search by name/keyword
    List<Item> findByNameContainingIgnoreCase(String keyword);
}
