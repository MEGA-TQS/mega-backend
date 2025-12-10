package com.sportsgear.rentalplatform.boundary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.dto.BlockDateDto;
import com.sportsgear.rentalplatform.dto.ItemCreateDto;
import com.sportsgear.rentalplatform.dto.ItemPriceUpdateDto;
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

    // US2: View Item Details
    // GET /api/items/1
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemDetails(@PathVariable Long id) {
        Optional<Item> item = itemService.getItemById(id);
        
        return item.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // US6: Create Equipment Listing
    // POST /api/items
    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody @Valid ItemCreateDto request) {
        try {
            Item createdItem = itemService.createItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // 400 se Owner não existir
        }
    }

    // US7: Atualizar Preço
    // PATCH /api/items/{id}/price?ownerId=1
    @PatchMapping("/{id}/price")
    public ResponseEntity<Item> updatePrice(
            @PathVariable Long id,
            @RequestParam Long ownerId,
            @RequestBody @Valid ItemPriceUpdateDto dto) {
        try {
            Item updatedItem = itemService.updatePrice(id, dto.getNewPrice(), ownerId);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build(); // Forbidden se não for dono
        }
    }

    // US7: Bloquear Datas
    // POST /api/items/{id}/block?ownerId=1
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockDates(
            @PathVariable Long id,
            @RequestParam Long ownerId,
            @RequestBody @Valid BlockDateDto dto) {
        try {
            itemService.blockDates(id, dto.getStartDate(), dto.getEndDate(), ownerId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build(); // 400 se já estiver ocupado
        }
    }
}
