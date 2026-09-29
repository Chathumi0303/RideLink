package com.ridelink.ridemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard structured error payload returned by all API endpoints")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred in UTC ISO-8601 format", example = "2026-09-29T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "409")
    private int status;

    @Schema(description = "Standardized error code classification", example = "RIDE_STATE_CONFLICT")
    private String error;

    @Schema(description = "Detailed explanatory message", example = "Ride cannot be started because its current status is REQUESTED")
    private String message;

    @Schema(description = "Target API request URI that caused the error", example = "/api/rides/550e8400-e29b-41d4-a716-446655440000/start")
    private String path;

    @Schema(description = "Field-level validation error details (if applicable)")
    private Map<String, String> validationErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(Instant timestamp, int status, String error, String message, String path,
                         Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
