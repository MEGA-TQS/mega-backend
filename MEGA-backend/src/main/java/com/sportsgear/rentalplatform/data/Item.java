package com.sportsgear.rentalplatform.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(length = 2000)
    private String description;
    
    private String category;
    private String location;

    private BigDecimal pricePerDay;

    // Instant Booking Support
    private boolean instantBookable;

    // If true, item is active. If false, it's "Paused" for the season.
    private boolean active; 

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
