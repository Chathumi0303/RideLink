package com.ridelink.ridemanagement.controller;

import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.response.ErrorResponse;
import com.ridelink.ridemanagement.dto.response.RideResponse;
import com.ridelink.ridemanagement.security.SecurityUtils;
import com.ridelink.ridemanagement.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API controller for the Ride Management Service.
 * Manages ride creation, querying, driver assignment, and lifecycle state changes.
 */
@RestController
@RequestMapping("/api/rides")
@Tag(name = "Ride Management", description = "Endpoints for managing rides and lifecycle state transitions")
public class RideController {

    private final RideService rideService;
    private final SecurityUtils securityUtils;

    public RideController(RideService rideService, SecurityUtils securityUtils) {
        this.rideService = rideService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(summary = "Create a new ride request", description = "Allows an authenticated passenger to submit a ride request with pickup and destination details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ride request created successfully",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failure or malformed payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_PASSENGER or ROLE_ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        String passengerId = securityUtils.getCurrentUserId();
        RideResponse response = rideService.createRide(request, passengerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Retrieve ride by ID", description = "Fetches complete details of a specific ride. Restricted to the passenger owner, assigned driver, or admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to view this ride",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> getRideById(
            @Parameter(description = "UUID of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String rideId) {
        String userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        RideResponse response = rideService.getRideById(rideId, userId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Retrieve current user's rides", description = "Returns ride history for the authenticated user based on role (passenger or driver).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rides retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<RideResponse>> getMyRides() {
        String userId = securityUtils.getCurrentUserId();
        boolean isDriver = securityUtils.isDriver();
        boolean isPassenger = securityUtils.isPassenger();
        List<RideResponse> responses = rideService.getMyRides(userId, isDriver, isPassenger);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Retrieve rides for a passenger", description = "Returns all rides requested by a given passenger. Accessible only by the passenger themselves or an administrator.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rides retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot view another passenger's rides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<RideResponse>> getRidesByPassenger(
            @Parameter(description = "Passenger identifier", example = "user-p101")
            @PathVariable String passengerId) {
        String userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        List<RideResponse> responses = rideService.getRidesByPassengerId(passengerId, userId, isAdmin);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Retrieve rides assigned to a driver", description = "Returns all rides assigned to a driver. Accessible only by the driver themselves or an administrator.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rides retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot view another driver's rides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<RideResponse>> getRidesByDriver(
            @Parameter(description = "Driver identifier", example = "driver-d202")
            @PathVariable String driverId) {
        String userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        List<RideResponse> responses = rideService.getRidesByDriverId(driverId, userId, isAdmin);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{rideId}/assign")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(summary = "Assign eligible driver to ride", description = "Queries Driver & Vehicle Service for eligible nearby drivers, selects the closest/optimal driver, and transitions status from REQUESTED to ASSIGNED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver successfully assigned",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "No available driver or invalid current status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Driver & Vehicle Service unavailable or failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> assignDriver(
            @Parameter(description = "UUID of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String rideId) {
        String userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        RideResponse response = rideService.assignDriver(rideId, userId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{rideId}/accept")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Accept assigned ride", description = "Executed by the assigned driver to accept the ride request. Status transitions from ASSIGNED to ACCEPTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride accepted by driver",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "403", description = "Driver not authorized to accept this ride",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid ride state transition",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> acceptRide(
            @Parameter(description = "UUID of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String rideId) {
        String driverId = securityUtils.getCurrentUserId();
        RideResponse response = rideService.acceptRide(rideId, driverId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{rideId}/start")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Start ride upon passenger pickup", description = "Executed by the assigned driver when the trip begins. Status transitions from ACCEPTED to IN_PROGRESS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride started and now in progress",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "403", description = "Driver not authorized to start this ride",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid ride state transition",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> startRide(
            @Parameter(description = "UUID of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String rideId) {
        String driverId = securityUtils.getCurrentUserId();
        RideResponse response = rideService.startRide(rideId, driverId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{rideId}/complete")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(summary = "Complete ride and trigger payment", description = "Executed by the driver upon reaching destination. Integrates with Fare & Payment Service to calculate final fare and confirm payment. Status transitions from IN_PROGRESS to COMPLETED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride completed and fare finalized",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "403", description = "Driver not authorized to complete this ride",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid ride state transition",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Fare & Payment Service failure",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> completeRide(
            @Parameter(description = "UUID of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String rideId) {
        String driverId = securityUtils.getCurrentUserId();
        RideResponse response = rideService.completeRide(rideId, driverId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{rideId}/cancel")
    @Operation(summary = "Cancel a ride", description = "Cancels a ride in REQUESTED, ASSIGNED, or ACCEPTED state. Allowed actors: requesting passenger, assigned driver, or admin. Terminal/in-progress rides cannot be cancelled.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride successfully cancelled",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for cancellation reason",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized to cancel this ride",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ride cannot be cancelled from current state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> cancelRide(
            @Parameter(description = "UUID of the ride", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String rideId,
            @Valid @RequestBody CancelRideRequest request) {
        String userId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();
        RideResponse response = rideService.cancelRide(rideId, userId, isAdmin, request);
        return ResponseEntity.ok(response);
    }
}
