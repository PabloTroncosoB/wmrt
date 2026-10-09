package com.wmrt.dispatch.repository;

import com.wmrt.dispatch.model.Reservation;
import com.wmrt.dispatch.model.ReservationStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Reservation data access using JdbcTemplate.
 */
@Repository
public class ReservationRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Reservation> reservationRowMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("id"),
            rs.getString("order_id"),
            rs.getLong("time_block_id"),
            rs.getString("customer_name"),
            rs.getString("address"),
            ReservationStatus.valueOf(rs.getString("status")),
            LocalDateTime.parse(rs.getString("created_at"))
    );

    public ReservationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Saves a new reservation and returns it with its generated ID.
     */
    public Reservation save(Reservation r) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO reservations (order_id, time_block_id, customer_name, address, status, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, r.getOrderId());
            ps.setLong(2, r.getTimeBlockId());
            ps.setString(3, r.getCustomerName());
            ps.setString(4, r.getAddress());
            ps.setString(5, r.getStatus().name());
            ps.setString(6, r.getCreatedAt().toString());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            r.setId(key.longValue());
        }
        return r;
    }

    /**
     * Returns a reservation by its ID.
     */
    public Optional<Reservation> findById(Long id) {
        List<Reservation> results = jdbc.query(
                "SELECT id, order_id, time_block_id, customer_name, address, status, created_at " +
                        "FROM reservations WHERE id = ?",
                reservationRowMapper,
                id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Returns a reservation by its order ID.
     */
    public Optional<Reservation> findByOrderId(String orderId) {
        List<Reservation> results = jdbc.query(
                "SELECT id, order_id, time_block_id, customer_name, address, status, created_at " +
                        "FROM reservations WHERE order_id = ?",
                reservationRowMapper,
                orderId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Returns all reservations.
     */
    public List<Reservation> findAll() {
        return jdbc.query(
                "SELECT id, order_id, time_block_id, customer_name, address, status, created_at " +
                        "FROM reservations ORDER BY id",
                reservationRowMapper
        );
    }

    /**
     * Updates the status of a reservation.
     */
    public void updateStatus(Long id, ReservationStatus status) {
        jdbc.update(
                "UPDATE reservations SET status = ? WHERE id = ?",
                status.name(),
                id
        );
    }

    /**
     * Returns true if a reservation exists for the given order ID.
     */
    public boolean existsByOrderId(String orderId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM reservations WHERE order_id = ?",
                Integer.class,
                orderId
        );
        return count != null && count > 0;
    }
}
