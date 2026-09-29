package com.ridelink.ridemanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload containing reason for cancelling a ride")
public class CancelRideRequest {

    @Schema(description = "Reason for ride cancellation", example = "Passenger requested change of plans")
    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 3, max = 255, message = "Cancellation reason must be between 3 and 255 characters")
    private String reason;

    public CancelRideRequest() {
    }

    public CancelRideRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
