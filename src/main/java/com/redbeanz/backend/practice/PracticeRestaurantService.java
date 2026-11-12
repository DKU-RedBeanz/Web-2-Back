package com.redbeanz.backend.practice;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PracticeRestaurantService {
    private final PracticeRestaurantRepository restaurantRepository;

    public PracticeRestaurantService(PracticeRestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public Optional<PracticeRestaurantResponse> findById(Long id) {
        return restaurantRepository.findById(id)
            .map(restaurant -> new PracticeRestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhoneNumber(),
                restaurant.getDescription(),
                restaurant.getThumbnailUrl()
            ));
    }
}
