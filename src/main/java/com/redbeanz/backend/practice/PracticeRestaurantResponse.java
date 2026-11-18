package com.redbeanz.backend.practice;

public record PracticeRestaurantResponse(
    Long id,
    String name,
    String address,
    String phoneNumber,
    String description,
    String thumbnailUrl
) {
}
