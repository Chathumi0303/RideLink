package lk.sliit.it3130.farepayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FinalFareRequest(
        @NotNull(message = "rideId is required")
        UUID rideId,

        @DecimalMin(value = "0.1", message = "distanceKm must be greater than 0")
        double distanceKm,

        @Min(value = 0, message = "durationMinutes cannot be negative")
        int durationMinutes
) {}
