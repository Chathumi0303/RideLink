package com.ridelink.ridemanagement.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the distinct states in the auditable Ride lifecycle.
 * Transitions are strictly controlled by the domain state machine.
 */
public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    private static final Set<RideStatus> FROM_REQUESTED = EnumSet.of(ASSIGNED, CANCELLED);
    private static final Set<RideStatus> FROM_ASSIGNED = EnumSet.of(ACCEPTED, CANCELLED);
    private static final Set<RideStatus> FROM_ACCEPTED = EnumSet.of(IN_PROGRESS, CANCELLED);
    private static final Set<RideStatus> FROM_IN_PROGRESS = EnumSet.of(COMPLETED);
    private static final Set<RideStatus> FROM_COMPLETED = Collections.emptySet();
    private static final Set<RideStatus> FROM_CANCELLED = Collections.emptySet();

    /**
     * Checks whether this status can transition to the target status.
     *
     * @param target the target state
     * @return true if valid transition, false otherwise
     */
    public boolean canTransitionTo(RideStatus target) {
        if (target == null) {
            return false;
        }
        return switch (this) {
            case REQUESTED -> FROM_REQUESTED.contains(target);
            case ASSIGNED -> FROM_ASSIGNED.contains(target);
            case ACCEPTED -> FROM_ACCEPTED.contains(target);
            case IN_PROGRESS -> FROM_IN_PROGRESS.contains(target);
            case COMPLETED -> FROM_COMPLETED.contains(target);
            case CANCELLED -> FROM_CANCELLED.contains(target);
        };
    }

    /**
     * Checks if the current state can be cancelled.
     * Only REQUESTED, ASSIGNED, and ACCEPTED rides can be cancelled.
     */
    public boolean isCancellable() {
        return this == REQUESTED || this == ASSIGNED || this == ACCEPTED;
    }

    /**
     * Checks if the ride has reached a terminal state.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
