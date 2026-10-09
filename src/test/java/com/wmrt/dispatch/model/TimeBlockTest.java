package com.wmrt.dispatch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeBlock domain logic.
 * No Spring context needed — pure Java.
 */
@DisplayName("TimeBlock")
class TimeBlockTest {

    private TimeBlock block(int capacity, int reserved) {
        return new TimeBlock(1L, 2L, LocalDate.now(), "09:00", "12:00", capacity, reserved, 5990.0);
    }

    // ── availableSlots() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("availableSlots() returns capacity minus reservedCount")
    void availableSlots_returnsRemainingCapacity() {
        assertEquals(7, block(10, 3).availableSlots());
    }

    @Test
    @DisplayName("availableSlots() returns 0 when fully booked")
    void availableSlots_returnsZeroWhenFull() {
        assertEquals(0, block(5, 5).availableSlots());
    }

    @Test
    @DisplayName("availableSlots() never returns negative")
    void availableSlots_neverNegative() {
        // reservedCount > capacity should not produce negative value
        assertEquals(0, block(5, 6).availableSlots());
    }

    @Test
    @DisplayName("availableSlots() returns full capacity when nothing reserved")
    void availableSlots_fullCapacityWhenEmpty() {
        assertEquals(10, block(10, 0).availableSlots());
    }

    // ── isFull() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isFull() returns false when slots remain")
    void isFull_falseWhenSlotsRemain() {
        assertFalse(block(10, 9).isFull());
    }

    @Test
    @DisplayName("isFull() returns true when reservedCount equals capacity")
    void isFull_trueWhenAtCapacity() {
        assertTrue(block(10, 10).isFull());
    }

    @Test
    @DisplayName("isFull() returns true when reservedCount exceeds capacity")
    void isFull_trueWhenOverCapacity() {
        assertTrue(block(5, 6).isFull());
    }

    @Test
    @DisplayName("isFull() returns false when block is empty")
    void isFull_falseWhenEmpty() {
        assertFalse(block(10, 0).isFull());
    }
}
