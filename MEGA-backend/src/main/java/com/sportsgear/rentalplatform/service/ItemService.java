package com.sportsgear.rentalplatform.service;

import java.math.BigDecimal; 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sportsgear.rentalplatform.data.Booking;
import com.sportsgear.rentalplatform.data.BookingItem;
import com.sportsgear.rentalplatform.data.BookingRepository;
import com.sportsgear.rentalplatform.data.BookingStatus;
import com.sportsgear.rentalplatform.data.Item;
import com.sportsgear.rentalplatform.data.ItemRepository;
import com.sportsgear.rentalplatform.data.UserRepository;
import com.sportsgear.rentalplatform.dto.ItemCreateDTO;

import jakarta.transaction.Transactional;

import com.sportsgear.rentalplatform.data.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public List<Item> search(
            String keyword, 
            String category, 
            String location,
            LocalDate startDate, 
            LocalDate endDate,   
            BigDecimal minPrice, 
            BigDecimal maxPrice) { 


        return itemRepository.searchItems(keyword, category, location, startDate, endDate, minPrice, maxPrice);
    }
    
    // US2: Get Details
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public List<Item> getItemsByOwner(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId);
    }

    // US6: Create Listing
    public Item createItem(ItemCreateDTO dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .location(dto.getLocation())
                .pricePerDay(dto.getPricePerDay())
                .condition(dto.getCondition())      
                .imageUrl(dto.getImageUrl())        
                .technicalSpecs(dto.getTechnicalSpecs())
                .pickupRules(dto.getPickupRules())
                .owner(owner)                       
                .active(true)                       
                .build();

        return itemRepository.save(item);
    }

    // US7: Update Price
    @Transactional
    public Item updatePrice(Long itemId, BigDecimal newPrice, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Only the owner can update the price.");
        }

        item.setPricePerDay(newPrice);
        return itemRepository.save(item);
    }

    // US7: Block Dates 
    @Transactional
    public void blockDates(Long itemId, LocalDate start, LocalDate end, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Only the owner can block dates.");
        }

        boolean hasConflict = bookingRepository.existsOverlappingBookings(
                Arrays.asList(itemId), start, end
        );

        if (hasConflict) {
            throw new IllegalStateException("Dates are already occupied by a booking.");
        }

        Booking block = Booking.builder()
                .renter(item.getOwner()) 
                .startDate(start)
                .endDate(end)
                .status(BookingStatus.BLOCKED) 
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BookingItem bookingItem = BookingItem.builder()
                .booking(block)
                .item(item)
                .priceAtBooking(BigDecimal.ZERO)
                .build();

        block.getItems().add(bookingItem);
        
        bookingRepository.save(block);
    }

    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        
        // Disable the item so it stops appearing in search
        item.setActive(false); 
        itemRepository.save(item);
    }
}