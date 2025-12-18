package com.sportsgear.rentalplatform.data;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- Availability Tests ---

    @Test
    @Tag("US-1")
    void searchItems_ShouldExcludeBookedItems_WhenDatesOverlap() {
        // 1. Setup Data
        User owner = User.builder().email("owner@test.com").name("Owner").build();
        entityManager.persist(owner);

        Item surfboard = Item.builder()
                .name("Surfboard")
                .category("Water")
                .location("Lisbon")
                .pricePerDay(BigDecimal.valueOf(20))
                .active(true)
                .owner(owner)
                .build();
        entityManager.persist(surfboard);

        // 2. Create a Booking (July 10 to July 15)
        Booking booking = Booking.builder()
                .renter(owner)
                .startDate(LocalDate.of(2025, 7, 10))
                .endDate(LocalDate.of(2025, 7, 15))
                .status(BookingStatus.APPROVED)
                .build();
        entityManager.persist(booking);

        // Link Item to Booking
        BookingItem link = BookingItem.builder()
                .booking(booking)
                .item(surfboard)
                .priceAtBooking(BigDecimal.valueOf(20))
                .build();
        entityManager.persist(link);

        // 3. Search during the booking (July 12-13) -> Should NOT match
        List<Item> resultsOverlap = itemRepository.searchItems(
                null, "Water", null,
                LocalDate.of(2025, 7, 12), LocalDate.of(2025, 7, 13),
                null, null
        );

        assertThat(resultsOverlap).isEmpty();

        // 4. Search after the booking (August 1-5) -> Should match
        List<Item> resultsFree = itemRepository.searchItems(
                null, "Water", null,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 5),
                null, null
        );

        assertThat(resultsFree).hasSize(1);
        assertThat(resultsFree.get(0).getName()).isEqualTo("Surfboard");
    }

    // --- Filter Tests ---

    @Test
    @Tag("US-1")
    void searchItems_ShouldFilterByPriceAndCategory() {
        // Setup
        User owner = User.builder().email("owner2@test.com").name("Owner").build();
        entityManager.persist(owner);

        Item cheapKayak = Item.builder().name("Kayak").category("Water").pricePerDay(BigDecimal.valueOf(15)).active(true).owner(owner).build();
        Item expensiveBike = Item.builder().name("Pro Bike").category("Cycling").pricePerDay(BigDecimal.valueOf(100)).active(true).owner(owner).build();
        
        entityManager.persist(cheapKayak);
        entityManager.persist(expensiveBike);

        // Test Category Filter
        List<Item> waterItems = itemRepository.searchItems(null, "Water", null, null, null, null, null);
        assertThat(waterItems).extracting(Item::getName).containsExactly("Kayak");

        // Test Price Filter (Max 50)
        List<Item> cheapItems = itemRepository.searchItems(null, null, null, null, null, null, BigDecimal.valueOf(50));
        assertThat(cheapItems).extracting(Item::getName).containsExactly("Kayak");
    }
}