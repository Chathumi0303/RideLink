package com.ridelink.ridemanagement.client.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * Request payload sent to Fare & Payment Service upon ride completion.
 */
public class FareCalculationRequestDto implements Serializable {

    private String rideId;
    private String passengerId;
    private String driverId;
    private Double distanceKm;
    private Long durationMinutes;
    private Instant startedAt;
    private Instant completedAt;

    public FareCalculationRequestDto() {
    }

    public FareCalculationRequestDto(String rideId, String passengerId, String driverId,
                                     Double distanceKm, Long durationMinutes,
                                     Instant startedAt, Instant completedAt) {
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
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

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
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
}
