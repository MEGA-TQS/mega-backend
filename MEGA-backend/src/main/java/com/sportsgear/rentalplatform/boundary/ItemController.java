package com.sportsgear.rentalplatform.boundary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.dto.BlockDateDTO;
import com.sportsgear.rentalplatform.dto.ItemCreateDTO;
import com.sportsgear.rentalplatform.dto.ItemPriceUpdateDTO;
import com.sportsgear.rentalplatform.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<Item> searchItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location, 
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        // Nota: Passar os novos filtros para o Service
        return itemService.search(keyword, category, location, startDate, endDate, minPrice, maxPrice);
    }

    // View item details
    // GET /api/items/1
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemDetails(@PathVariable Long id) {
        Optional<Item> item = itemService.getItemById(id);
        
        return item.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // Create item listing
    // POST /api/items
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody @Valid ItemCreateDTO request) {
        try {
            Item createdItem = itemService.createItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 400 se Owner não existir
        }
    }

    // Update price
    // PATCH /api/items/{id}/price?ownerId=1
    @PatchMapping("/{id}/price")
    public ResponseEntity<Item> updatePrice(
            @PathVariable Long id,
            @RequestParam Long ownerId,
            @RequestBody @Valid ItemPriceUpdateDTO dto) {
        try {
            Item updatedItem = itemService.updatePrice(id, dto.getNewPrice(), ownerId);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build(); // Forbidden se não for dono
        }
    }

    // Block dates
    // POST /api/items/{id}/block?ownerId=1
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockDates(
            @PathVariable Long id,
            @RequestParam Long ownerId,
            @RequestBody @Valid BlockDateDTO dto) {
        try {
            itemService.blockDates(id, dto.getStartDate(), dto.getEndDate(), ownerId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build(); // 400 se já estiver ocupado
        }
    }

    @GetMapping("/owner/{ownerId}")
    public List<Item> getItemsByOwner(@PathVariable Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    @PostMapping("/{itemId}/reviews")
    public ResponseEntity<com.sportsgear.rentalplatform.data.Review> addReview(
            @PathVariable Long itemId, 
            @RequestBody com.sportsgear.rentalplatform.dto.ReviewDTO reviewDto) {
        try {
            com.sportsgear.rentalplatform.data.Review review = itemService.addReview(itemId, reviewDto);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
