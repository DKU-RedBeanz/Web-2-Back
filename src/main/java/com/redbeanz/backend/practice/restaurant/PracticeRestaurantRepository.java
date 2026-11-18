package com.redbeanz.backend.practice.restaurant;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PracticeRestaurantRepository {

    private final JdbcClient jdbcClient;

    public PracticeRestaurantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<RestaurantDetailResponse> findById(long id) {
        return jdbcClient.sql("""
                SELECT id, name, address, phone_number, description, thumbnail_url
                FROM restaurants
                WHERE id = :id
                """)
                .param("id", id)
                .query((row, rowNumber) -> new RestaurantDetailResponse(
                        row.getLong("id"),
                        row.getString("name"),
                        row.getString("address"),
                        row.getString("phone_number"),
                        row.getString("description"),
                        row.getString("thumbnail_url")
                ))
                .optional();
    }
}
