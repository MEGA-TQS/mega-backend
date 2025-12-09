package com.sportsgear.rentalplatform.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "renter_id")
    private User renter;

    private LocalDate startDate;
    private LocalDate endDate;

    // Calculated as sum of all BookingItems
    private BigDecimal totalPrice; 

    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, APPROVED, PAID, CANCELLED

    // One transaction, multiple items
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingItem> items;
}
