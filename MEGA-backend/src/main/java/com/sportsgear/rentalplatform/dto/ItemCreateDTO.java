package com.sportsgear.rentalplatform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemCreateDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal pricePerDay;

    private String imageUrl;

    @NotBlank(message = "Condition is required")
    private String condition;

    private String technicalSpecs;
    private String pickupRules;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    private boolean instantBookable;
}