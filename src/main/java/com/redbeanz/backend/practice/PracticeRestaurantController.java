package com.redbeanz.backend.practice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api/practice/restaurants")
public class PracticeRestaurantController {
    private final PracticeRestaurantService restaurantService;

    public PracticeRestaurantController(PracticeRestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PracticeRestaurantResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.of(restaurantService.findById(id));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Void> invalidId() {
        return ResponseEntity.badRequest().build();
    }
}
