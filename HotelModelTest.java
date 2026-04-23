package com.grandhotel;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Room and Customer data models.
 * Run with: mvn test
 */
class HotelModelTest {

    // ===== Room Tests =====

    @Test
    @DisplayName("New room should be available by default")
    void roomAvailableOnCreation() {
        Main.Room room = new Main.Room(101, "Single", 1500.0, true);
        assertTrue(room.isAvailable(), "Newly created room must be available");
    }

    @Test
    @DisplayName("Room status text reflects availability")
    void roomStatusText() {
        Main.Room room = new Main.Room(102, "Double", 2500.0, true);
        assertTrue(room.getStatus().startsWith("[OK]"), "Available room status must start with [OK]");

        room.setAvailable(false);
        assertTrue(room.getStatus().startsWith("[X]"), "Booked room status must start with [X]");
    }

    @Test
    @DisplayName("Room fields are stored correctly")
    void roomFieldValues() {
        Main.Room room = new Main.Room(205, "Suite", 8000.0, true);
        assertEquals(205,    room.getRoomNumber());
        assertEquals("Suite", room.getType());
        assertEquals(8000.0, room.getPrice(), 0.001);
    }

    @Test
    @DisplayName("setAvailable toggles availability")
    void roomToggleAvailability() {
        Main.Room room = new Main.Room(301, "Deluxe", 4000.0, true);
        room.setAvailable(false);
        assertFalse(room.isAvailable());
        room.setAvailable(true);
        assertTrue(room.isAvailable());
    }

    // ===== Customer Tests =====

    @Test
    @DisplayName("Customer fields are stored correctly")
    void customerFieldValues() {
        Main.Customer c = new Main.Customer("Aarav Shah", "9876543210", 101, "01-01-2025 10:00", 3000.0);
        assertEquals("Aarav Shah",    c.getName());
        assertEquals("9876543210",    c.getContact());
        assertEquals(101,             c.getRoomNumber());
        assertEquals("01-01-2025 10:00", c.getCheckIn());
        assertEquals(3000.0,          c.getTotalBill(), 0.001);
    }

    @Test
    @DisplayName("Customer totalBill matches room price")
    void customerBillMatchesRoomPrice() {
        double price = 5500.0;
        Main.Room     room = new Main.Room(402, "Suite", price, true);
        Main.Customer cust = new Main.Customer("Priya Nair", "email@test.com",
                room.getRoomNumber(), "09-04-2026 14:00", room.getPrice());

        assertEquals(room.getPrice(), cust.getTotalBill(), 0.001,
                "Customer bill should equal the room's nightly price");
    }
}
