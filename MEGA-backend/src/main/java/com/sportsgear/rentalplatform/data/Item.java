package com.sportsgear.rentalplatform.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

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

    @Column(columnDefinition = "TEXT")
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 1000)
    private String technicalSpecs; 

    private String imageUrl; 

    private String pickupRules;

    private String condition; 

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<Review> reviews; 
    
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

    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }
}
