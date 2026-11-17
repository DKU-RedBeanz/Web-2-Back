package com.redbeanz.backend.practice.restaurant;

public record RestaurantDetailResponse(
        long id,
        String name,
        String address,
        String phoneNumber,
        String description,
        String thumbnailUrl
) {
}
