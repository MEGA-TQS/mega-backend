package com.sportsgear.rentalplatform.service;

import com.sportsgear.rentalplatform.data.*;
import com.sportsgear.rentalplatform.dto.ItemCreateDTO;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
    @Tag("US-1")
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
    @Tag("US-6")
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
    @Tag("US-6")
    void createItem_ShouldThrowException_WhenOwnerNotFound() {
        Long ownerId = 99L;
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setOwnerId(ownerId);

        assertThrows(IllegalArgumentException.class, () -> itemService.createItem(dto));
    }

    @Test
    @Tag("US-6")
    void whenCreateItem_thenSaveIsCalled() {
        ItemCreateDTO dto = new ItemCreateDTO();
        dto.setName("Bike");
        dto.setOwnerId(1L);
        dto.setPricePerDay(BigDecimal.TEN);
        
        User owner = new User();
        owner.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        Item created = itemService.createItem(dto);

        assertNotNull(created);
        assertEquals("Bike", created.getName());
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void getItemById_ShouldReturnItem_WhenFound() {
        Long id = 1L;
        Item item = Item.builder().id(id).name("Kayak").build();
        
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        
        Optional<Item> result = itemService.getItemById(id);
        
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Kayak");
        verify(itemRepository).findById(id);
    }
    // --- Update Price Tests ---

    @Test
    @Tag("US-7")
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
    @Tag("US-7")
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
    @Tag("US-7")
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
    @Tag("US-7")
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

    @Test
    @Tag("US-7")
    void whenBlockDatesWithConflict_thenThrowException() {
        Long itemId = 1L;
        Long ownerId = 1L;
        
        Item item = Item.builder().id(itemId).owner(User.builder().id(ownerId).build()).build();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Simula conflito
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> 
            itemService.blockDates(itemId, LocalDate.now(), LocalDate.now().plusDays(1), ownerId)
        );
    }
    // Delete Tests
    @Test
    @Tag("US-6")
    void deleteItem_ShouldSoftDelete_WhenItemExists() {
        // Given
        Long itemId = 1L;
        // Start with active = true
        Item item = Item.builder().id(itemId).active(true).build();
        
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        itemService.deleteItem(itemId);

        // Then
        assertThat(item.isActive()).isFalse(); // Critical check: flag must be false
        verify(itemRepository).save(item);     // Must save the change
    }

    @Test
    @Tag("US-6")
    void deleteItem_ShouldThrowException_WhenItemNotFound() {
        Long itemId = 99L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> itemService.deleteItem(itemId));
    }

    @Test
    @Tag("US-11")
    void whenOwnerBlocksWinterSeason_thenRangeIsBlocked() {
        // GIVEN: Um item e um intervalo "Sazonal" (3 meses)
        Long itemId = 1L;
        Long ownerId = 5L;
        LocalDate startWinter = LocalDate.of(2025, 12, 1);
        LocalDate endWinter = LocalDate.of(2026, 2, 28);

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        // Não há conflitos existentes, podemos bloquear
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false);

        // WHEN
        itemService.blockDates(itemId, startWinter, endWinter, ownerId);

        // THEN: Verifica se foi guardado um bloqueio com as datas corretas e status BLOCKED
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        Booking savedBlock = bookingCaptor.getValue();
        assertEquals(BookingStatus.BLOCKED, savedBlock.getStatus());
        assertEquals(startWinter, savedBlock.getStartDate());
        assertEquals(endWinter, savedBlock.getEndDate());
    }

    @Test
    void addReview_ShouldSucceed_WhenUserAndItemExist() {
        // GIVEN
        Long itemId = 1L;
        Long userId = 2L;
        
        Item item = Item.builder().id(itemId).build();
        User user = User.builder().id(userId).build();
        
        com.sportsgear.rentalplatform.dto.ReviewDTO dto = new com.sportsgear.rentalplatform.dto.ReviewDTO();
        dto.setReviewerId(userId);
        dto.setRating(5);
        dto.setComment("Great!");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // WHEN
        com.sportsgear.rentalplatform.data.Review result = itemService.addReview(itemId, dto);

        // THEN
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great!", result.getComment());
        assertEquals(user, result.getReviewer());
        
        verify(itemRepository).save(item);
    }
}