package com.sportsgear.rentalplatform.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String phoneNumber;
    private String address;
}