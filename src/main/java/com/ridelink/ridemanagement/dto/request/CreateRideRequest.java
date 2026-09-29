package com.ridelink.ridemanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request body for creating a new ride request")
public class CreateRideRequest {

    @Schema(description = "Pickup location details")
    @NotNull(message = "Pickup location is required")
    @Valid
    private LocationRequest pickup;

    @Schema(description = "Destination location details")
    @NotNull(message = "Destination location is required")
    @Valid
    private LocationRequest destination;

    @Schema(description = "Optional notes or instructions for the driver", example = "Please call upon arrival at gate 2")
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Schema(description = "Estimated fare obtained from Fare & Payment service (optional)", example = "450.00")
    @DecimalMin(value = "0.0", inclusive = true, message = "Estimated fare must be non-negative")
    private BigDecimal estimatedFare;

    @Schema(description = "Estimated distance in kilometers (optional)", example = "7.8")
    @DecimalMin(value = "0.0", inclusive = true, message = "Distance must be non-negative")
    private Double distanceKm;

    public CreateRideRequest() {
    }

    public CreateRideRequest(LocationRequest pickup, LocationRequest destination, String notes) {
        this.pickup = pickup;
        this.destination = destination;
        this.notes = notes;
    }

    public CreateRideRequest(LocationRequest pickup, LocationRequest destination, String notes,
                             BigDecimal estimatedFare, Double distanceKm) {
        this.pickup = pickup;
        this.destination = destination;
        this.notes = notes;
        this.estimatedFare = estimatedFare;
        this.distanceKm = distanceKm;
    }

    public LocationRequest getPickup() {
        return pickup;
    }

    public void setPickup(LocationRequest pickup) {
        this.pickup = pickup;
    }

    public LocationRequest getDestination() {
        return destination;
    }

    public void setDestination(LocationRequest destination) {
        this.destination = destination;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(BigDecimal estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
