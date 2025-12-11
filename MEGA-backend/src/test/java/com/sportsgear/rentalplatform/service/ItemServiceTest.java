package com.sportsgear.rentalplatform.service;

import com.sportsgear.rentalplatform.data.*;
import com.sportsgear.rentalplatform.dto.ItemCreateDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemService itemService;

    // Search Tests

    @Test
    void search_ShouldDelegateToRepository() {
        String category = "Water";
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(2);

        when(itemRepository.searchItems(any(), eq(category), any(), eq(start), eq(end), any(), any()))
                .thenReturn(Collections.emptyList());

        itemService.search(null, category, null, start, end, null, null);

        verify(itemRepository).searchItems(null, category, null, start, end, null, null);
    }

    // Create Item Tests

    @Test
    void createItem_ShouldSucceed_WhenOwnerExists() {
        Long ownerId = 10L;
        User owner = User.builder().id(ownerId).name("Alberto").build();

        // Expect findById since you pass the ID explicitly now
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setOwnerId(ownerId);
        dto.setName("Surfboard");
        dto.setDescription("Good condition");
        dto.setCategory("Water");
        dto.setLocation("Lisbon");
        dto.setPricePerDay(BigDecimal.TEN);
        dto.setCondition("New");

        Item result = itemService.createItem(dto);

        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getName()).isEqualTo("Surfboard");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void createItem_ShouldThrowException_WhenOwnerNotFound() {
        Long ownerId = 99L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setOwnerId(ownerId);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(dto));
    }

    // --- Update Price Tests ---

    @Test
    void updatePrice_ShouldSucceed_WhenCallerIsOwner() {
        Long itemId = 1L;
        Long ownerId = 5L;
        
        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).pricePerDay(BigDecimal.TEN).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        Item updated = itemService.updatePrice(itemId, BigDecimal.valueOf(20), ownerId);

        assertThat(updated.getPricePerDay()).isEqualTo(BigDecimal.valueOf(20));
    }

    @Test
    void updatePrice_ShouldThrowException_WhenCallerIsNotOwner() {
        Long itemId = 1L;
        Long ownerId = 5L;
        Long wrongId = 99L;

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalStateException.class, 
            () -> itemService.updatePrice(itemId, BigDecimal.valueOf(20), wrongId));
    }

    // --- Block Dates Tests ---

    @Test
    void blockDates_ShouldSucceed_WhenNoOverlap() {
        Long itemId = 1L;
        Long ownerId = 5L;
        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false);

        itemService.blockDates(itemId, LocalDate.now(), LocalDate.now().plusDays(2), ownerId);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void blockDates_ShouldThrowException_WhenDatesOverlap() {
        Long itemId = 1L;
        Long ownerId = 5L;
        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(true);

        assertThrows(IllegalStateException.class, 
            () -> itemService.blockDates(itemId, LocalDate.now(), LocalDate.now().plusDays(2), ownerId));
    }
}