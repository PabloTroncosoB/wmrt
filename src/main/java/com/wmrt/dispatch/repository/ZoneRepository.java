package com.wmrt.dispatch.repository;

import com.wmrt.dispatch.model.Zone;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Zone data access using JdbcTemplate.
 */
@Repository
public class ZoneRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Zone> zoneRowMapper = (rs, rowNum) -> new Zone(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("region"),
            rs.getString("coverage_description")
    );

    public ZoneRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns all zones.
     */
    public List<Zone> findAll() {
        return jdbc.query("SELECT id, name, region, coverage_description FROM zones ORDER BY id", zoneRowMapper);
    }

    /**
     * Returns a zone by its ID, or empty if not found.
     */
    public Optional<Zone> findById(Long id) {
        List<Zone> results = jdbc.query(
                "SELECT id, name, region, coverage_description FROM zones WHERE id = ?",
                zoneRowMapper,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
