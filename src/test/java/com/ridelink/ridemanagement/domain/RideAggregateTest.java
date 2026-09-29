package com.ridelink.ridemanagement.domain;

import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.exception.UnauthorizedRideAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ride Aggregate Root Lifecycle and Authorization Unit Tests")
class RideAggregateTest {

    private Ride ride;
    private final String passengerId = "passenger-001";
    private final String driverId = "driver-001";
    private final String otherDriverId = "driver-999";
    private final String otherUserId = "user-unknown";

    @BeforeEach
    void setUp() {
        Location pickup = new Location("Pickup Spot", 6.9271, 79.8612);
        Location destination = new Location("Drop Spot", 6.9344, 79.8428);
        ride = Ride.createNewRequest(passengerId, pickup, destination, "Urgent trip",
                BigDecimal.valueOf(500.0), 5.5);
    }

    @Test
    @DisplayName("Creation initializes expected default state and timestamps")
    void testCreation() {
        assertNotNull(ride.getId());
        assertEquals(passengerId, ride.getPassengerId());
        assertEquals(RideStatus.REQUESTED, ride.getStatus());
        assertNotNull(ride.getRequestedAt());
        assertNotNull(ride.getCreatedAt());
        assertNotNull(ride.getUpdatedAt());
        assertNull(ride.getDriverId());
    }

    @Test
    @DisplayName("Lifecycle Happy Path: REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED")
    void testHappyPathLifecycle() {
        // 1. Assign Driver
        ride.assignDriver(driverId);
        assertEquals(RideStatus.ASSIGNED, ride.getStatus());
        assertEquals(driverId, ride.getDriverId());
        assertNotNull(ride.getAssignedAt());

        // 2. Accept
        ride.accept(driverId);
        assertEquals(RideStatus.ACCEPTED, ride.getStatus());
        assertNotNull(ride.getAcceptedAt());

        // 3. Start
        ride.start(driverId);
        assertEquals(RideStatus.IN_PROGRESS, ride.getStatus());
        assertNotNull(ride.getStartedAt());

        // 4. Complete
        BigDecimal finalFare = BigDecimal.valueOf(520.0);
        ride.complete(driverId, finalFare);
        assertEquals(RideStatus.COMPLETED, ride.getStatus());
        assertEquals(finalFare, ride.getFinalFare());
        assertNotNull(ride.getCompletedAt());
    }

    @Test
    @DisplayName("Driver assignment fails if ride is already in-progress or terminal")
    void testAssignDriverInvalidState() {
        ride.assignDriver(driverId);
        ride.accept(driverId);
        ride.start(driverId);

        // Cannot assign when IN_PROGRESS
        assertThrows(InvalidRideStateException.class, () -> ride.assignDriver("driver-002"));
    }

    @Test
    @DisplayName("Wrong driver cannot accept assigned ride")
    void testAcceptByWrongDriverThrowsUnauthorized() {
        ride.assignDriver(driverId);
        assertThrows(UnauthorizedRideAccessException.class, () -> ride.accept(otherDriverId));
    }

    @Test
    @DisplayName("Cannot accept ride if not in ASSIGNED state")
    void testAcceptInvalidState() {
        // Status is still REQUESTED
        assertThrows(InvalidRideStateException.class, () -> ride.accept(driverId));
    }

    @Test
    @DisplayName("Wrong driver cannot start ride")
    void testStartByWrongDriverThrowsUnauthorized() {
        ride.assignDriver(driverId);
        ride.accept(driverId);
        assertThrows(UnauthorizedRideAccessException.class, () -> ride.start(otherDriverId));
    }

    @Test
    @DisplayName("Cannot start ride if not in ACCEPTED state")
    void testStartInvalidState() {
        // Still REQUESTED
        assertThrows(InvalidRideStateException.class, () -> ride.start(driverId));
    }

    @Test
    @DisplayName("Wrong driver cannot complete ride")
    void testCompleteByWrongDriverThrowsUnauthorized() {
        ride.assignDriver(driverId);
        ride.accept(driverId);
        ride.start(driverId);
        assertThrows(UnauthorizedRideAccessException.class, () ->
                ride.complete(otherDriverId, BigDecimal.valueOf(500.0)));
    }

    @Test
    @DisplayName("Cannot complete ride if not IN_PROGRESS")
    void testCompleteInvalidState() {
        ride.assignDriver(driverId);
        ride.accept(driverId);
        // Status is ACCEPTED, not IN_PROGRESS
        assertThrows(InvalidRideStateException.class, () ->
                ride.complete(driverId, BigDecimal.valueOf(500.0)));
    }

    @Test
    @DisplayName("Passenger can cancel REQUESTED ride")
    void testPassengerCancelRequestedRide() {
        ride.cancel(passengerId, false, "Changed plans");
        assertEquals(RideStatus.CANCELLED, ride.getStatus());
        assertEquals("Changed plans", ride.getCancellationReason());
        assertNotNull(ride.getCancelledAt());
    }

    @Test
    @DisplayName("Assigned driver can cancel ASSIGNED ride")
    void testAssignedDriverCancel() {
        ride.assignDriver(driverId);
        ride.cancel(driverId, false, "Flat tire emergency");
        assertEquals(RideStatus.CANCELLED, ride.getStatus());
    }

    @Test
    @DisplayName("Admin can cancel ride in cancellable state")
    void testAdminCancel() {
        ride.cancel("admin-user", true, "System maintenance cancellation");
        assertEquals(RideStatus.CANCELLED, ride.getStatus());
    }

    @Test
    @DisplayName("Unrelated user cannot cancel ride")
    void testUnrelatedUserCancelThrowsUnauthorized() {
        assertThrows(UnauthorizedRideAccessException.class, () ->
                ride.cancel(otherUserId, false, "Malicious attempt"));
    }

    @Test
    @DisplayName("Cannot cancel completed ride")
    void testCancelCompletedRideThrowsConflict() {
        ride.assignDriver(driverId);
        ride.accept(driverId);
        ride.start(driverId);
        ride.complete(driverId, BigDecimal.valueOf(500.0));

        assertThrows(InvalidRideStateException.class, () ->
                ride.cancel(passengerId, false, "Late cancellation attempt"));
    }

    @Test
    @DisplayName("Cannot cancel an already cancelled ride")
    void testCancelAlreadyCancelledRideThrowsConflict() {
        ride.cancel(passengerId, false, "Initial cancel");
        assertThrows(InvalidRideStateException.class, () ->
                ride.cancel(passengerId, false, "Second cancel"));
    }
}
