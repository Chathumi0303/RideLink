package com.ridelink.ridemanagement.domain;

import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.exception.UnauthorizedRideAccessException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Core aggregate root representing a Ride in the RideLink platform.
 * Persisted in the independent 'rides' collection inside 'ride_management_db'.
 */
@Document(collection = "rides")
public class Ride implements Serializable {

    @Id
    private String id;

    @Indexed
    private String passengerId;

    @Indexed
    private String driverId;

    private Location pickup;
    private Location destination;

    @Indexed
    private RideStatus status;

    @Indexed
    private Instant requestedAt;

    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    private String cancellationReason;
    private BigDecimal estimatedFare;
    private BigDecimal finalFare;
    private Double distanceKm;
    private String notes;

    private Instant createdAt;
    private Instant updatedAt;

    public Ride() {
    }

    /**
     * Factory method to create a new ride request initiated by a passenger.
     */
    public static Ride createNewRequest(String passengerId, Location pickup, Location destination,
                                       String notes, BigDecimal estimatedFare, Double distanceKm) {
        Ride ride = new Ride();
        ride.id = UUID.randomUUID().toString();
        ride.passengerId = passengerId;
        ride.pickup = pickup;
        ride.destination = destination;
        ride.notes = notes;
        ride.estimatedFare = estimatedFare;
        ride.distanceKm = (distanceKm != null && distanceKm > 0) ? distanceKm :
                (pickup != null && destination != null ? pickup.distanceTo(destination) : 0.0);
        ride.status = RideStatus.REQUESTED;
        Instant now = Instant.now();
        ride.requestedAt = now;
        ride.createdAt = now;
        ride.updatedAt = now;
        return ride;
    }

    // =========================================================================
    // Domain Business Lifecycle Methods (Encapsulated State Machine)
    // =========================================================================

    /**
     * Assigns a driver to this ride request.
     */
    public void assignDriver(String driverId) {
        if (!this.status.canTransitionTo(RideStatus.ASSIGNED)) {
            throw new InvalidRideStateException(
                    String.format("Cannot assign driver to ride '%s' because its current status is %s", this.id, this.status)
            );
        }
        if (driverId == null || driverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty for assignment");
        }
        this.driverId = driverId;
        this.status = RideStatus.ASSIGNED;
        Instant now = Instant.now();
        this.assignedAt = now;
        this.updatedAt = now;
    }

    /**
     * Allows the assigned driver to accept the ride.
     */
    public void accept(String requestingDriverId) {
        if (!this.status.canTransitionTo(RideStatus.ACCEPTED)) {
            throw new InvalidRideStateException(
                    String.format("Cannot accept ride '%s' because its current status is %s", this.id, this.status)
            );
        }
        if (!Objects.equals(this.driverId, requestingDriverId)) {
            throw new UnauthorizedRideAccessException(
                    String.format("Driver '%s' is not authorized to accept ride '%s' assigned to '%s'",
                            requestingDriverId, this.id, this.driverId)
            );
        }
        this.status = RideStatus.ACCEPTED;
        Instant now = Instant.now();
        this.acceptedAt = now;
        this.updatedAt = now;
    }

    /**
     * Starts the ride once the driver picks up the passenger.
     */
    public void start(String requestingDriverId) {
        if (!this.status.canTransitionTo(RideStatus.IN_PROGRESS)) {
            throw new InvalidRideStateException(
                    String.format("Cannot start ride '%s' because its current status is %s", this.id, this.status)
            );
        }
        if (!Objects.equals(this.driverId, requestingDriverId)) {
            throw new UnauthorizedRideAccessException(
                    String.format("Driver '%s' is not authorized to start ride '%s'", requestingDriverId, this.id)
            );
        }
        this.status = RideStatus.IN_PROGRESS;
        Instant now = Instant.now();
        this.startedAt = now;
        this.updatedAt = now;
    }

    /**
     * Completes the ride after passenger dropoff and processes final fare.
     */
    public void complete(String requestingDriverId, BigDecimal finalFare) {
        if (!this.status.canTransitionTo(RideStatus.COMPLETED)) {
            throw new InvalidRideStateException(
                    String.format("Cannot complete ride '%s' because its current status is %s", this.id, this.status)
            );
        }
        if (!Objects.equals(this.driverId, requestingDriverId)) {
            throw new UnauthorizedRideAccessException(
                    String.format("Driver '%s' is not authorized to complete ride '%s'", requestingDriverId, this.id)
            );
        }
        this.status = RideStatus.COMPLETED;
        this.finalFare = finalFare;
        Instant now = Instant.now();
        this.completedAt = now;
        this.updatedAt = now;
    }

    /**
     * Cancels the ride if it is in a cancellable state.
     * Allowed actors: the passenger owner, the assigned driver, or an admin.
     */
    public void cancel(String requestingUserId, boolean isAdmin, String reason) {
        boolean isOwnerPassenger = Objects.equals(this.passengerId, requestingUserId);
        boolean isAssignedDriver = Objects.equals(this.driverId, requestingUserId);

        if (!isAdmin && !isOwnerPassenger && !isAssignedDriver) {
            throw new UnauthorizedRideAccessException(
                    String.format("User '%s' is not authorized to cancel ride '%s'", requestingUserId, this.id)
            );
        }

        if (!this.status.isCancellable()) {
            throw new InvalidRideStateException(
                    String.format("Cannot cancel ride '%s' because its current status is %s. Terminal or in-progress rides cannot be cancelled.",
                            this.id, this.status)
            );
        }

        this.status = RideStatus.CANCELLED;
        this.cancellationReason = reason;
        Instant now = Instant.now();
        this.cancelledAt = now;
        this.updatedAt = now;
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Location getPickup() {
        return pickup;
    }

    public void setPickup(Location pickup) {
        this.pickup = pickup;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public BigDecimal getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(BigDecimal estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public BigDecimal getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(BigDecimal finalFare) {
        this.finalFare = finalFare;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return Objects.equals(id, ride.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ride{" +
                "id='" + id + '\'' +
                ", passengerId='" + passengerId + '\'' +
                ", driverId='" + driverId + '\'' +
                ", status=" + status +
                ", requestedAt=" + requestedAt +
                ", finalFare=" + finalFare +
                '}';
    }
}
