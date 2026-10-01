package org.example.payment.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProcessedEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProcessedEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean claim(UUID eventId) {
        try {
            jdbcTemplate.update("INSERT INTO processed_events(service_name, event_id) VALUES (?, ?)",
                    "payment", eventId);
            return true;
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }
}
