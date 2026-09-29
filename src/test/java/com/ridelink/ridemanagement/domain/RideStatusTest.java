package com.ridelink.ridemanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RideStatus State Machine Unit Tests")
class RideStatusTest {

    @Test
    @DisplayName("REQUESTED state valid transitions")
    void requestedTransitions() {
        assertTrue(RideStatus.REQUESTED.canTransitionTo(RideStatus.ASSIGNED));
        assertTrue(RideStatus.REQUESTED.canTransitionTo(RideStatus.CANCELLED));

        assertFalse(RideStatus.REQUESTED.canTransitionTo(RideStatus.ACCEPTED));
        assertFalse(RideStatus.REQUESTED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertFalse(RideStatus.REQUESTED.canTransitionTo(RideStatus.COMPLETED));
        assertFalse(RideStatus.REQUESTED.canTransitionTo(null));
    }

    @Test
    @DisplayName("ASSIGNED state valid transitions")
    void assignedTransitions() {
        assertTrue(RideStatus.ASSIGNED.canTransitionTo(RideStatus.ACCEPTED));
        assertTrue(RideStatus.ASSIGNED.canTransitionTo(RideStatus.CANCELLED));

        assertFalse(RideStatus.ASSIGNED.canTransitionTo(RideStatus.REQUESTED));
        assertFalse(RideStatus.ASSIGNED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertFalse(RideStatus.ASSIGNED.canTransitionTo(RideStatus.COMPLETED));
    }

    @Test
    @DisplayName("ACCEPTED state valid transitions")
    void acceptedTransitions() {
        assertTrue(RideStatus.ACCEPTED.canTransitionTo(RideStatus.IN_PROGRESS));
        assertTrue(RideStatus.ACCEPTED.canTransitionTo(RideStatus.CANCELLED));

        assertFalse(RideStatus.ACCEPTED.canTransitionTo(RideStatus.REQUESTED));
        assertFalse(RideStatus.ACCEPTED.canTransitionTo(RideStatus.ASSIGNED));
        assertFalse(RideStatus.ACCEPTED.canTransitionTo(RideStatus.COMPLETED));
    }

    @Test
    @DisplayName("IN_PROGRESS state valid transitions")
    void inProgressTransitions() {
        assertTrue(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.COMPLETED));

        assertFalse(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.CANCELLED));
        assertFalse(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.REQUESTED));
        assertFalse(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.ASSIGNED));
        assertFalse(RideStatus.IN_PROGRESS.canTransitionTo(RideStatus.ACCEPTED));
    }

    @ParameterizedTest
    @EnumSource(RideStatus.class)
    @DisplayName("COMPLETED is terminal state - no transitions allowed")
    void completedIsTerminal(RideStatus target) {
        assertFalse(RideStatus.COMPLETED.canTransitionTo(target));
    }

    @ParameterizedTest
    @EnumSource(RideStatus.class)
    @DisplayName("CANCELLED is terminal state - no transitions allowed")
    void cancelledIsTerminal(RideStatus target) {
        assertFalse(RideStatus.CANCELLED.canTransitionTo(target));
    }

    @Test
    @DisplayName("Verify cancellable states")
    void cancellableStates() {
        assertTrue(RideStatus.REQUESTED.isCancellable());
        assertTrue(RideStatus.ASSIGNED.isCancellable());
        assertTrue(RideStatus.ACCEPTED.isCancellable());

        assertFalse(RideStatus.IN_PROGRESS.isCancellable());
        assertFalse(RideStatus.COMPLETED.isCancellable());
        assertFalse(RideStatus.CANCELLED.isCancellable());
    }

    @Test
    @DisplayName("Verify terminal states")
    void terminalStates() {
        assertTrue(RideStatus.COMPLETED.isTerminal());
        assertTrue(RideStatus.CANCELLED.isTerminal());

        assertFalse(RideStatus.REQUESTED.isTerminal());
        assertFalse(RideStatus.ASSIGNED.isTerminal());
        assertFalse(RideStatus.ACCEPTED.isTerminal());
        assertFalse(RideStatus.IN_PROGRESS.isTerminal());
    }
}
