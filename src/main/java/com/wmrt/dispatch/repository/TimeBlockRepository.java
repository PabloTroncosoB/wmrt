package com.wmrt.dispatch.repository;

import com.wmrt.dispatch.model.TimeBlock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for TimeBlock data access using JdbcTemplate.
 */
@Repository
public class TimeBlockRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<TimeBlock> timeBlockRowMapper = (rs, rowNum) -> new TimeBlock(
            rs.getLong("id"),
            rs.getLong("zone_id"),
            LocalDate.parse(rs.getString("available_date")),
            rs.getString("start_time"),
            rs.getString("end_time"),
            rs.getInt("capacity"),
            rs.getInt("reserved_count"),
            rs.getDouble("cost")
    );

    public TimeBlockRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Returns all time blocks for a given zone and date.
     */
    public List<TimeBlock> findByZoneAndDate(Long zoneId, LocalDate date) {
        return jdbc.query(
                "SELECT id, zone_id, available_date, start_time, end_time, capacity, reserved_count, cost " +
                        "FROM time_blocks WHERE zone_id = ? AND available_date = ? ORDER BY start_time",
                timeBlockRowMapper,
                zoneId,
                date.toString()
        );
    }

    /**
     * Returns all dates in the range [from, to] for the given zone that have at least one non-full block.
     */
    public List<LocalDate> findAvailableDatesByZone(Long zoneId, LocalDate from, LocalDate to) {
        List<String> dateStrings = jdbc.queryForList(
                "SELECT DISTINCT available_date FROM time_blocks " +
                        "WHERE zone_id = ? AND available_date >= ? AND available_date <= ? " +
                        "AND reserved_count < capacity " +
                        "ORDER BY available_date",
                String.class,
                zoneId,
                from.toString(),
                to.toString()
        );
        return dateStrings.stream().map(LocalDate::parse).toList();
    }

    /**
     * Returns a time block by its ID, or empty if not found.
     */
    public Optional<TimeBlock> findById(Long id) {
        List<TimeBlock> results = jdbc.query(
                "SELECT id, zone_id, available_date, start_time, end_time, capacity, reserved_count, cost " +
                        "FROM time_blocks WHERE id = ?",
                timeBlockRowMapper,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Increments the reserved_count for the given time block (called when creating a reservation).
     * SQLite does not support true SELECT FOR UPDATE, so concurrency is handled at the service layer.
     */
    public void incrementReservedCount(Long id) {
        jdbc.update(
                "UPDATE time_blocks SET reserved_count = reserved_count + 1 WHERE id = ?",
                id
        );
    }

    /**
     * Decrements the reserved_count for the given time block (called when cancelling a reservation).
     */
    public void decrementReservedCount(Long id) {
        jdbc.update(
                "UPDATE time_blocks SET reserved_count = MAX(0, reserved_count - 1) WHERE id = ?",
                id
        );
    }

    /**
     * Fetches a time block for update purposes.
     * SQLite does not support SELECT FOR UPDATE; we rely on a synchronized block in the service layer.
     */
    public Optional<TimeBlock> findByIdForUpdate(Long id) {
        return findById(id);
    }

    /**
     * Returns the count of blocks for a given zone, date, and start_time (used by DataSeeder).
     */
    public int countByZoneDateStartTime(Long zoneId, LocalDate date, String startTime) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM time_blocks WHERE zone_id = ? AND available_date = ? AND start_time = ?",
                Integer.class,
                zoneId,
                date.toString(),
                startTime
        );
        return count != null ? count : 0;
    }

    /**
     * Inserts a new time block.
     */
    public void insert(Long zoneId, LocalDate date, String startTime, String endTime,
                       int capacity, double cost) {
        jdbc.update(
                "INSERT INTO time_blocks (zone_id, available_date, start_time, end_time, capacity, reserved_count, cost) " +
                        "VALUES (?, ?, ?, ?, ?, 0, ?)",
                zoneId,
                date.toString(),
                startTime,
                endTime,
                capacity,
                cost
        );
    }
}
