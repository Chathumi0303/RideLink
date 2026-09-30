package lk.sliit.it3130.farepayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FareEstimateRequest(
        @DecimalMin(value = "0.1", message = "distanceKm must be greater than 0")
        double distanceKm,

        @Min(value = 0, message = "durationMinutes cannot be negative")
        @Max(value = 600, message = "durationMinutes is too large")
        int durationMinutes
) {}
