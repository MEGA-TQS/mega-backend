package com.sportsgear.rentalplatform.data;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequest {

    @NotNull(message = "Renter ID is required")
    private Long renterId;

    // Group Booking - We accept a LIST of Item IDs
    @NotEmpty(message = "At least one item must be selected")
    private List<Long> itemIds; 

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future") // SQA Validation
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
}
