package com.ridelink.ridemanagement.dto.response;

import com.ridelink.ridemanagement.domain.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Detailed representation of a Ride resource")
public class RideResponse {

    @Schema(description = "Unique identifier of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Identifier of the passenger who created the ride", example = "user-p101")
    private String passengerId;

    @Schema(description = "Identifier of the assigned driver (null if not yet assigned)", example = "driver-d202")
    private String driverId;

    @Schema(description = "Pickup location details")
    private LocationResponse pickup;

    @Schema(description = "Destination location details")
    private LocationResponse destination;

    @Schema(description = "Current lifecycle status of the ride", example = "REQUESTED")
    private RideStatus status;

    @Schema(description = "Timestamp when the ride request was submitted", example = "2026-09-29T10:15:30Z")
    private Instant requestedAt;

    @Schema(description = "Timestamp when a driver was assigned", example = "2026-09-29T10:16:00Z")
    private Instant assignedAt;

    @Schema(description = "Timestamp when the assigned driver accepted", example = "2026-09-29T10:16:45Z")
    private Instant acceptedAt;

    @Schema(description = "Timestamp when the ride started", example = "2026-09-29T10:20:00Z")
    private Instant startedAt;

    @Schema(description = "Timestamp when the ride was completed", example = "2026-09-29T10:45:00Z")
    private Instant completedAt;

    @Schema(description = "Timestamp when the ride was cancelled (if applicable)")
    private Instant cancelledAt;

    @Schema(description = "Reason provided for cancellation", example = "Passenger changed plans")
    private String cancellationReason;

    @Schema(description = "Estimated fare calculated prior to or at ride creation", example = "450.00")
    private BigDecimal estimatedFare;

    @Schema(description = "Final calculated fare upon completion from Fare & Payment service", example = "480.00")
    private BigDecimal finalFare;

    @Schema(description = "Distance in kilometers", example = "7.8")
    private Double distanceKm;

    @Schema(description = "Special notes or instructions for driver", example = "Please call on arrival")
    private String notes;

    @Schema(description = "Resource creation timestamp")
    private Instant createdAt;

    @Schema(description = "Resource last update timestamp")
    private Instant updatedAt;

    public RideResponse() {
    }

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

    public LocationResponse getPickup() {
        return pickup;
    }

    public void setPickup(LocationResponse pickup) {
        this.pickup = pickup;
    }

    public LocationResponse getDestination() {
        return destination;
    }

    public void setDestination(LocationResponse destination) {
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
}
