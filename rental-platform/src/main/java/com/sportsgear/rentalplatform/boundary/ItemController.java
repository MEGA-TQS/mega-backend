package com.sportsgear.rentalplatform.boundary;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.sportsgear.rentalplatform.data.Item;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    
    // GET /api/items?location=Lisbon&sport=Surf
    @GetMapping
    public List<Item> searchItems(...) { ... }

    // POST /api/items
    @PostMapping
    public Item createItem(...) { ... }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public Item getItemDetails(...) { ... }
}
