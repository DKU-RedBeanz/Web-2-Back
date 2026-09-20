package com.redbeanz.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// Uses the application's real DataSource and root .env; no replacement database.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MySqlConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void selectOneReturnsOne() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        assertThat(result).isEqualTo(1);
    }
}
