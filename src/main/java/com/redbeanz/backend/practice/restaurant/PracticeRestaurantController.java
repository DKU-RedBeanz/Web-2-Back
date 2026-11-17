package com.redbeanz.backend.practice.restaurant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/practice/restaurants")
public class PracticeRestaurantController {

    private final PracticeRestaurantRepository restaurantRepository;

    public PracticeRestaurantController(PracticeRestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailResponse> findById(@PathVariable("id") long id) {
        return ResponseEntity.of(restaurantRepository.findById(id));
    }
}
