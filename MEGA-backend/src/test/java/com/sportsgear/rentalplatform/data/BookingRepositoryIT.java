package com.sportsgear.rentalplatform.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryIT {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- US3 & US6: Availability Logic Tests ---

    @Test
    void existsOverlappingBookings_ShouldReturnTrue_WhenDatesOverlap() {
        // Setup Data
        User renter = User.builder().email("renter@test.com").name("Renter").build();
        entityManager.persist(renter);

        Item kayak = Item.builder()
                .name("Kayak")
                .pricePerDay(BigDecimal.TEN)
                .active(true)
                .build();
        entityManager.persist(kayak);

        // Create Existing Booking (Jan 10 to Jan 20)
        Booking booking = Booking.builder()
                .renter(renter)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 1, 20))
                .status(BookingStatus.APPROVED)
                .build();
        entityManager.persist(booking);

        BookingItem link = BookingItem.builder().booking(booking).item(kayak).build();
        entityManager.persist(link);

        // Test Scenarios
        
        // Scenario A: Request dates are INSIDE existing booking (Jan 12-15)
        boolean overlapInside = bookingRepository.existsOverlappingBookings(
                List.of(kayak.getId()), 
                LocalDate.of(2025, 1, 12), 
                LocalDate.of(2025, 1, 15)
        );
        assertThat(overlapInside).isTrue();

        // Scenario B: Request dates SURROUND existing booking (Jan 1-30)
        boolean overlapSurround = bookingRepository.existsOverlappingBookings(
                List.of(kayak.getId()), 
                LocalDate.of(2025, 1, 1), 
                LocalDate.of(2025, 1, 30)
        );
        assertThat(overlapSurround).isTrue();

        // Scenario C: Request dates PARTIALLY overlap (Jan 18-25)
        boolean overlapPartial = bookingRepository.existsOverlappingBookings(
                List.of(kayak.getId()), 
                LocalDate.of(2025, 1, 18), 
                LocalDate.of(2025, 1, 25)
        );
        assertThat(overlapPartial).isTrue();
    }

    @Test
    void existsOverlappingBookings_ShouldReturnFalse_WhenDatesDoNotOverlap() {
        // Setup (Same as above)
        User renter = User.builder().email("renter2@test.com").name("Renter").build();
        entityManager.persist(renter);
        Item kayak = Item.builder().name("Kayak").pricePerDay(BigDecimal.TEN).active(true).build();
        entityManager.persist(kayak);

        Booking booking = Booking.builder()
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 1, 20))
                .status(BookingStatus.APPROVED)
                .build();
        entityManager.persist(booking);
        entityManager.persist(BookingItem.builder().booking(booking).item(kayak).build());

        // Test Scenario: Request is strictly BEFORE (Jan 1-5)
        boolean isBefore = bookingRepository.existsOverlappingBookings(
                List.of(kayak.getId()), 
                LocalDate.of(2025, 1, 1), 
                LocalDate.of(2025, 1, 5)
        );
        assertThat(isBefore).isFalse();

        // Test Scenario: Request is strictly AFTER (Jan 25-30)
        boolean isAfter = bookingRepository.existsOverlappingBookings(
                List.of(kayak.getId()), 
                LocalDate.of(2025, 1, 25), 
                LocalDate.of(2025, 1, 30)
        );
        assertThat(isAfter).isFalse();
    }

    @Test
    void existsOverlappingBookings_ShouldIgnoreCancelledBookings() {
        // Setup
        User renter = User.builder().email("renter3@test.com").name("Renter").build();
        entityManager.persist(renter);
        Item kayak = Item.builder().name("Kayak").pricePerDay(BigDecimal.TEN).active(true).build();
        entityManager.persist(kayak);

        // Create a CANCELLED booking for the same dates
        Booking cancelledBooking = Booking.builder()
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 1, 20))
                .status(BookingStatus.CANCELLED) // <--- Critical Check
                .build();
        entityManager.persist(cancelledBooking);
        entityManager.persist(BookingItem.builder().booking(cancelledBooking).item(kayak).build());

        // Test: Should allow booking because the conflict is cancelled
        boolean hasConflict = bookingRepository.existsOverlappingBookings(
                List.of(kayak.getId()), 
                LocalDate.of(2025, 1, 10), 
                LocalDate.of(2025, 1, 20)
        );
        assertThat(hasConflict).isFalse();
    }

    // History Tests

    @Test
    void findByRenterId_ShouldReturnUserBookings() {
        // Setup Users
        User renter1 = User.builder().email("r1@test.com").name("R1").build();
        User renter2 = User.builder().email("r2@test.com").name("R2").build();
        entityManager.persist(renter1);
        entityManager.persist(renter2);

        // Booking for Renter 1
        Booking b1 = Booking.builder().renter(renter1).status(BookingStatus.PENDING).build();
        entityManager.persist(b1);

        // Booking for Renter 2
        Booking b2 = Booking.builder().renter(renter2).status(BookingStatus.PENDING).build();
        entityManager.persist(b2);

        // Test
        List<Booking> r1Bookings = bookingRepository.findByRenterId(renter1.getId());
        
        assertThat(r1Bookings).hasSize(1);
        assertThat(r1Bookings.get(0).getRenter()).isEqualTo(renter1);
    }
}