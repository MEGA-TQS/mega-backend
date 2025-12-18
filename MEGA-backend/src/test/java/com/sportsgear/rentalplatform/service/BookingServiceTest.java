package com.sportsgear.rentalplatform.service;

import com.sportsgear.rentalplatform.data.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private BookingService bookingService;

    private User renter;
    private User owner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        renter = User.builder().id(1L).name("Renter").build();
        owner = User.builder().id(2L).name("Owner").build();
        
        item = Item.builder()
                .id(10L)
                .name("Bike")
                .pricePerDay(BigDecimal.TEN)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(100L)
                .renter(renter)
                .status(BookingStatus.PENDING)
                .items(Collections.singletonList(BookingItem.builder().item(item).build()))
                .build();
    }

    @Test
    @Tag("US-3")
    void whenDatesOverlap_thenThrowException() {
        // GIVEN
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(renter));
        when(itemRepository.findAllById(anyList())).thenReturn(Arrays.asList(item));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(true);

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> bookingService.createGroupBooking(req));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @Tag("US-4")
    void whenOwnerAccepts_thenStatusApproved() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.acceptBooking(100L, 2L);

        // THEN
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    @Tag("US-4")
    void whenAcceptNonPendingBooking_thenThrowException() {
        // GIVEN: Booking is Cancelled
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            bookingService.acceptBooking(100L, 2L));
        
        assertEquals("Only pending bookings can be accepted.", ex.getMessage());
    }

    @Test
    @Tag("US-4")
    void whenOwnerDeclines_thenStatusRejected() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.declineBooking(100L, 2L); // Owner ID 2

        // THEN
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    @Tag("US-4")
    void whenDeclineNonPendingBooking_thenThrowException() {
        // GIVEN: Booking is already APPROVED
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            bookingService.declineBooking(100L, 2L));
        assertEquals("Only pending bookings can be declined.", ex.getMessage());
    }

    @Test
    @Tag("US-4")
    void whenNonOwnerDeclines_thenThrowException() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN: User 99 tries to decline
        assertThrows(IllegalStateException.class, () -> 
            bookingService.declineBooking(100L, 99L));
    }

    @Test
    @Tag("US-4")
    void whenWrongOwnerTriesToAccept_thenThrowException() {
        // GIVEN
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> bookingService.acceptBooking(100L, 99L));
    }

    @Test
    @Tag("US-8")
    void whenAllItemsAreInstantBookable_thenStatusIsApproved() {
        // GIVEN
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Collections.singletonList(10L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        Item instantItem = Item.builder()
                .id(10L)
                .pricePerDay(BigDecimal.TEN)
                .instantBookable(true) 
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(instantItem));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createGroupBooking(req);

        // THEN
        assertEquals(BookingStatus.APPROVED, result.getStatus()); 
    }

    @Test
    @Tag("US-8")
    void whenItemIsNotInstant_thenStatusIsPending() {
        // GIVEN
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Collections.singletonList(10L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        Item normalItem = Item.builder()
                .id(10L)
                .pricePerDay(BigDecimal.TEN)
                .instantBookable(false) 
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(normalItem));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createGroupBooking(req);

        // THEN
        assertEquals(BookingStatus.PENDING, result.getStatus()); 
    }

    @Test
    @Tag("US-9")
    void whenBookingMonthsInAdvance_thenSuccess() {
        // GIVEN: Uma reserva para daqui a 6 meses
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Collections.singletonList(10L));
        req.setStartDate(LocalDate.now().plusMonths(6)); // Futuro distante
        req.setEndDate(LocalDate.now().plusMonths(6).plusDays(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(item));
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false); // Disponível
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createGroupBooking(req);

        // THEN
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus()); // AC: Request sent to owner
        assertEquals(req.getStartDate(), result.getStartDate());
    }

    @Test
    @Tag("US-9")
    void whenBookingTooFarInFuture_thenThrowException() {
        // GIVEN: Uma reserva para daqui a 2 anos (inválida pela nossa regra)
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Collections.singletonList(10L));
        req.setStartDate(LocalDate.now().plusYears(2)); 
        req.setEndDate(LocalDate.now().plusYears(2).plusDays(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createGroupBooking(req);
        });

        assertTrue(exception.getMessage().contains("up to 1 year"));
    }

    @Test
    @Tag("US-9")
    void whenBookingFutureDateAlreadyOccupied_thenThrowException() {
        // GIVEN: Uma tentativa de reserva para daqui a 3 meses
        BookingRequest req = new BookingRequest();
        req.setRenterId(2L);
        req.setItemIds(Collections.singletonList(10L));
        req.setStartDate(LocalDate.now().plusMonths(3));
        req.setEndDate(LocalDate.now().plusMonths(3).plusDays(5));

        // Mock dos repositórios
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(item));

        // O repositório diz que JÁ EXISTE uma reserva nessas datas
        when(bookingRepository.existsOverlappingBookings(
                anyList(), 
                eq(req.getStartDate()), 
                eq(req.getEndDate())
        )).thenReturn(true); 

        // WHEN & THEN
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bookingService.createGroupBooking(req);
        });

        // Verificamos se a mensagem de erro é a correta
        assertEquals("Selected items are not available for the requested dates.", exception.getMessage());
    }

    @Test
    @Tag("US-10")
    void whenBookingMultipleItems_thenTotalIsSumOfAll() {
        // GIVEN: Um pedido com 2 itens
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L, 20L)); // Dois itens
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3)); // 2 dias de aluguer

        // Mock Item 1 ( 20€/dia)
        Item item1 = Item.builder().id(10L).pricePerDay(new BigDecimal("20.00")).active(true).build();
        // Mock Item 2 (10€/dia)
        Item item2 = Item.builder().id(20L).pricePerDay(new BigDecimal("10.00")).active(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        // O repositório devolve os dois itens
        when(itemRepository.findAllById(req.getItemIds())).thenReturn(Arrays.asList(item1, item2));
        // Disponibilidade OK para ambos
        when(bookingRepository.existsOverlappingBookings(anyList(), any(), any())).thenReturn(false);
        // Capturar o que vai ser salvo
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.createGroupBooking(req);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.getItems().size()); // Tem 2 itens na lista

        BigDecimal expectedTotal = new BigDecimal("60.00"); 
        assertEquals(0, expectedTotal.compareTo(result.getTotalPrice())); 
    }

    @Test
    @Tag("US-3")
    void whenCreateBookingWithInvalidRenter_thenThrowException() {
        BookingRequest req = new BookingRequest();
        req.setRenterId(999L); // Invalid ID

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            bookingService.createGroupBooking(req));
    }

    @Test
    @Tag("US-3")
    void whenCreateBookingWithInvalidItems_thenThrowException() {
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L, 999L)); // 10 exists, 999 does not
        
        // FIX: Add dates to avoid NullPointerException
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        
        // Repo returns only 1 item (found), but we asked for 2
        when(itemRepository.findAllById(anyList())).thenReturn(Collections.singletonList(item));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> 
            bookingService.createGroupBooking(req));
        
        assertEquals("Invalid Item IDs provided. Some items do not exist.", ex.getMessage());
    }

    @Test
    @Tag("US-10")
    void whenOneItemOfMultipleIsUnavailable_thenFailTransaction() {
        // GIVEN: Pedido para 2 itens
        BookingRequest req = new BookingRequest();
        req.setRenterId(1L);
        req.setItemIds(Arrays.asList(10L, 20L));
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(itemRepository.findAllById(anyList())).thenReturn(Arrays.asList(new Item(), new Item()));

        // SIMULAÇÃO DO ERRO: O repositório diz que HÁ conflito (num deles ou nos dois)
        when(bookingRepository.existsOverlappingBookings(
                anyList(), any(), any()
        )).thenReturn(true); 

        // WHEN & THEN
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            bookingService.createGroupBooking(req);
        });

        assertEquals("Selected items are not available for the requested dates.", exception.getMessage());
    }

    @Test
    @Tag("US-3")
    void updateStatus_ShouldCancel_WhenRenterRequests() {
        // GIVEN
        User renter = User.builder().id(1L).build();
        
        Booking booking = Booking.builder()
                .id(100L)
                .renter(renter)
                .status(BookingStatus.PENDING)
                .startDate(LocalDate.now().plusDays(5)) // <--- CORREÇÃO: Data futura (obrigatório)
                .items(new java.util.ArrayList<>())     // (Mantém a lista que corrigimos antes)
                .build();
        
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Booking result = bookingService.updateStatus(100L, BookingStatus.CANCELLED, 1L);

        // THEN
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @Tag("US-3")
    void updateStatus_ShouldThrow_WhenRandomUserTriesToCancel() {
        // GIVEN
        User renter = User.builder().id(1L).build();
        User owner = User.builder().id(2L).build(); // Item owner
        Item item = Item.builder().owner(owner).build();
        
        Booking booking = Booking.builder()
                .id(100L)
                .renter(renter)
                .items(Collections.singletonList(BookingItem.builder().item(item).build()))
                .build();
        
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN: User 99 (Hacker) tries to cancel
        assertThrows(IllegalStateException.class, () -> 
            bookingService.updateStatus(100L, BookingStatus.CANCELLED, 99L));
    }

    @Test
    @Tag("US-5")
    void updateStatus_ShouldThrow_WhenTryingToSetPAIDManually() {
        // GIVEN
        Booking booking = Booking.builder().id(100L).build();
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> 
            bookingService.updateStatus(100L, BookingStatus.PAID, 1L));
    }

    @Test
    @Tag("US-4")
    void updateStatus_ShouldCancel_WhenOwnerRequests() {
        // GIVEN
        booking.setStartDate(LocalDate.now().plusDays(5)); // Future date
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN: Owner (ID 2) cancels
        Booking result = bookingService.updateStatus(100L, BookingStatus.CANCELLED, 2L);

        // THEN
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    @Tag("US-3")
    void updateStatus_ShouldThrow_WhenCancellingStartedBooking() {
        // GIVEN: Start date was yesterday
        booking.setStartDate(LocalDate.now().minusDays(1)); 
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        // WHEN & THEN
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
            bookingService.updateStatus(100L, BookingStatus.CANCELLED, 1L));
        
        assertEquals("Cannot cancel a booking that has already started.", ex.getMessage());
    }

    @Test
    @Tag("US-3")
    void getBookingsByRenter_ShouldReturnList() {
        when(bookingRepository.findByRenterId(1L)).thenReturn(Collections.singletonList(booking));
        
        var result = bookingService.getBookingsByRenter(1L);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @Tag("US-4")
    void getBookingsForOwner_ShouldReturnList() {
        when(bookingRepository.findBookingsByOwner(2L)).thenReturn(Collections.singletonList(booking));
        
        var result = bookingService.getBookingsForOwner(2L);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}