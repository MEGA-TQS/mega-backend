package com.sportsgear.rentalplatform.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private int rating;
    private String comment;
    private Long reviewerId;
}