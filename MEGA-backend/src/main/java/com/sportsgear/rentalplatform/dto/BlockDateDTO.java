package com.sportsgear.rentalplatform.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BlockDateDTO {
    @NotNull @FutureOrPresent
    private LocalDate startDate;
    
    @NotNull @FutureOrPresent
    private LocalDate endDate;
}