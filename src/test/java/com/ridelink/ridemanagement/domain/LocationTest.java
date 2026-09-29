package com.ridelink.ridemanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Location Value Object Unit Tests")
class LocationTest {

    @Test
    @DisplayName("Distance calculation between two known coordinates")
    void calculateDistance() {
        // University Entrance (approx 6.9271, 79.8612) to Colombo Fort (approx 6.9344, 79.8428)
        Location loc1 = new Location("University Entrance", 6.9271, 79.8612);
        Location loc2 = new Location("Colombo Fort", 6.9344, 79.8428);

        double distance = loc1.distanceTo(loc2);
        assertTrue(distance > 1.5 && distance < 3.0, "Calculated distance should be approx 2 km");
    }

    @Test
    @DisplayName("Distance to null returns zero")
    void distanceToNull() {
        Location loc = new Location("A", 6.9, 79.8);
        assertEquals(0.0, loc.distanceTo(null));
    }

    @Test
    @DisplayName("Distance with null coordinates returns zero")
    void distanceWithNullCoords() {
        Location loc1 = new Location("A", null, 79.8);
        Location loc2 = new Location("B", 6.9, 79.8);
        assertEquals(0.0, loc1.distanceTo(loc2));
    }
}
