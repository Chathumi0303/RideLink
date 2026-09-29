package com.ridelink.ridemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ridemanagement.config.SecurityConfig;
import com.ridelink.ridemanagement.domain.RideStatus;
import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.request.LocationRequest;
import com.ridelink.ridemanagement.dto.response.LocationResponse;
import com.ridelink.ridemanagement.dto.response.RideResponse;
import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.exception.NoDriverAvailableException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.exception.UnauthorizedRideAccessException;
import com.ridelink.ridemanagement.security.JwtRoleConverter;
import com.ridelink.ridemanagement.security.SecurityUtils;
import com.ridelink.ridemanagement.service.RideService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RideController.class)
@Import({SecurityConfig.class, JwtRoleConverter.class})
@DisplayName("RideController MockMvc API & Security Tests")
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtDecoder jwtDecoder;

    private final String rideId = "ride-uuid-123";
    private final String passengerId = "passenger-101";
    private final String driverId = "driver-202";

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides creates ride and returns 201 CREATED")
    void testCreateRideSuccess() throws Exception {
        CreateRideRequest request = new CreateRideRequest(
                new LocationRequest("University", 6.9271, 79.8612),
                new LocationRequest("Fort", 6.9344, 79.8428),
                "Luggage in boot"
        );

        when(securityUtils.getCurrentUserId()).thenReturn(passengerId);

        RideResponse mockResponse = createMockRideResponse(rideId, passengerId, null, RideStatus.REQUESTED);
        when(rideService.createRide(any(CreateRideRequest.class), eq(passengerId))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(rideId))
                .andExpect(jsonPath("$.passengerId").value(passengerId))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides with missing destination returns 400 BAD REQUEST")
    void testCreateRideValidationFailureMissingDestination() throws Exception {
        CreateRideRequest request = new CreateRideRequest();
        request.setPickup(new LocationRequest("University", 6.9271, 79.8612));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors.destination").exists());
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("GET /api/rides/passenger/{id} for other passenger returns 403 FORBIDDEN")
    void testGetOtherPassengerRidesReturns403() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("passenger-101");
        when(securityUtils.isAdmin()).thenReturn(false);
        when(rideService.getRidesByPassengerId("passenger-202", "passenger-101", false))
                .thenThrow(new UnauthorizedRideAccessException("You are not authorized to view another passenger's rides"));

        mockMvc.perform(get("/api/rides/passenger/passenger-202"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "driver-101", roles = {"DRIVER"})
    @DisplayName("GET /api/rides/driver/{id} for other driver returns 403 FORBIDDEN")
    void testGetOtherDriverRidesReturns403() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("driver-101");
        when(securityUtils.isAdmin()).thenReturn(false);
        when(rideService.getRidesByDriverId("driver-202", "driver-101", false))
                .thenThrow(new UnauthorizedRideAccessException("You are not authorized to view another driver's rides"));

        mockMvc.perform(get("/api/rides/driver/driver-202"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides with missing pickup returns 400 BAD REQUEST with validationErrors")
    void testCreateRideValidationFailureMissingPickup() throws Exception {
        CreateRideRequest request = new CreateRideRequest();
        request.setDestination(new LocationRequest("Fort", 6.9344, 79.8428));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors.pickup").exists());
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides with invalid coordinates returns 400 BAD REQUEST")
    void testCreateRideValidationFailureInvalidCoordinates() throws Exception {
        CreateRideRequest request = new CreateRideRequest(
                new LocationRequest("Invalid Lat", 95.0, 79.8612), // Lat > 90
                new LocationRequest("Fort", 6.9344, 79.8428),
                null
        );

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/rides without authentication returns 401 UNAUTHORIZED")
    void testCreateRideUnauthenticatedReturns401() throws Exception {
        CreateRideRequest request = new CreateRideRequest(
                new LocationRequest("University", 6.9271, 79.8612),
                new LocationRequest("Fort", 6.9344, 79.8428),
                null
        );

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "driver-202", roles = {"DRIVER"})
    @DisplayName("POST /api/rides with DRIVER role returns 403 FORBIDDEN (passengers only)")
    void testCreateRideForbiddenForDriver() throws Exception {
        CreateRideRequest request = new CreateRideRequest(
                new LocationRequest("University", 6.9271, 79.8612),
                new LocationRequest("Fort", 6.9344, 79.8428),
                null
        );

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("GET /api/rides/{id} returns 200 OK for owner")
    void testGetRideByIdSuccess() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(passengerId);
        when(securityUtils.isAdmin()).thenReturn(false);

        RideResponse mockResponse = createMockRideResponse(rideId, passengerId, null, RideStatus.REQUESTED);
        when(rideService.getRideById(rideId, passengerId, false)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/rides/{rideId}", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rideId));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("GET /api/rides/{id} for non-existent ride returns 404 NOT FOUND")
    void testGetRideByIdNotFound() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(passengerId);
        when(securityUtils.isAdmin()).thenReturn(false);

        when(rideService.getRideById(eq("missing-id"), anyString(), anyBoolean()))
                .thenThrow(new RideNotFoundException("Ride not found with id: 'missing-id'"));

        mockMvc.perform(get("/api/rides/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "stranger-999", roles = {"PASSENGER"})
    @DisplayName("GET /api/rides/{id} unauthorized access returns 403 FORBIDDEN")
    void testGetRideByIdUnauthorizedAccessReturns403() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("stranger-999");
        when(securityUtils.isAdmin()).thenReturn(false);

        when(rideService.getRideById(eq(rideId), eq("stranger-999"), eq(false)))
                .thenThrow(new UnauthorizedRideAccessException("You are not authorized to view ride: " + rideId));

        mockMvc.perform(get("/api/rides/{rideId}", rideId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides/{id}/assign successfully assigns driver")
    void testAssignDriverSuccess() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(passengerId);
        when(securityUtils.isAdmin()).thenReturn(false);

        RideResponse mockResponse = createMockRideResponse(rideId, passengerId, driverId, RideStatus.ASSIGNED);
        when(rideService.assignDriver(rideId, passengerId, false)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/rides/{rideId}/assign", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.driverId").value(driverId));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides/{id}/assign with no available driver returns 409 CONFLICT")
    void testAssignDriverNoDriverAvailableReturns409() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(passengerId);
        when(securityUtils.isAdmin()).thenReturn(false);

        when(rideService.assignDriver(rideId, passengerId, false))
                .thenThrow(new NoDriverAvailableException("No eligible drivers available in pickup area"));

        mockMvc.perform(post("/api/rides/{rideId}/assign", rideId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("NO_DRIVER_AVAILABLE"));
    }

    @Test
    @WithMockUser(username = "driver-202", roles = {"DRIVER"})
    @DisplayName("POST /api/rides/{id}/accept transitions to ACCEPTED")
    void testAcceptRideSuccess() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(driverId);

        RideResponse mockResponse = createMockRideResponse(rideId, passengerId, driverId, RideStatus.ACCEPTED);
        when(rideService.acceptRide(rideId, driverId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/rides/{rideId}/accept", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(username = "driver-202", roles = {"DRIVER"})
    @DisplayName("POST /api/rides/{id}/accept invalid state returns 409 CONFLICT")
    void testAcceptRideInvalidStateReturns409() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(driverId);

        when(rideService.acceptRide(rideId, driverId))
                .thenThrow(new InvalidRideStateException("Cannot accept ride because current status is REQUESTED"));

        mockMvc.perform(post("/api/rides/{rideId}/accept", rideId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("RIDE_STATE_CONFLICT"));
    }

    @Test
    @WithMockUser(username = "driver-202", roles = {"DRIVER"})
    @DisplayName("POST /api/rides/{id}/start transitions to IN_PROGRESS")
    void testStartRideSuccess() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(driverId);

        RideResponse mockResponse = createMockRideResponse(rideId, passengerId, driverId, RideStatus.IN_PROGRESS);
        when(rideService.startRide(rideId, driverId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/rides/{rideId}/start", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "driver-202", roles = {"DRIVER"})
    @DisplayName("POST /api/rides/{id}/complete transitions to COMPLETED")
    void testCompleteRideSuccess() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(driverId);

        RideResponse mockResponse = createMockRideResponse(rideId, passengerId, driverId, RideStatus.COMPLETED);
        mockResponse.setFinalFare(BigDecimal.valueOf(480.0));
        when(rideService.completeRide(rideId, driverId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/rides/{rideId}/complete", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.finalFare").value(480.0));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides/{id}/cancel with valid reason cancels ride")
    void testCancelRideSuccess() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn(passengerId);
        when(securityUtils.isAdmin()).thenReturn(false);

        CancelRideRequest cancelRequest = new CancelRideRequest("Passenger emergency cancellation");
        RideResponse mockResponse = createMockRideResponse(rideId, passengerId, null, RideStatus.CANCELLED);
        mockResponse.setCancellationReason("Passenger emergency cancellation");

        when(rideService.cancelRide(eq(rideId), eq(passengerId), eq(false), any(CancelRideRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/rides/{rideId}/cancel", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Passenger emergency cancellation"));
    }

    @Test
    @WithMockUser(username = "passenger-101", roles = {"PASSENGER"})
    @DisplayName("POST /api/rides/{id}/cancel with blank reason returns 400 BAD REQUEST")
    void testCancelRideBlankReasonReturns400() throws Exception {
        CancelRideRequest cancelRequest = new CancelRideRequest("");

        mockMvc.perform(post("/api/rides/{rideId}/cancel", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors.reason").exists());
    }

    private RideResponse createMockRideResponse(String id, String passengerId, String driverId, RideStatus status) {
        RideResponse response = new RideResponse();
        response.setId(id);
        response.setPassengerId(passengerId);
        response.setDriverId(driverId);
        response.setStatus(status);
        response.setPickup(new LocationResponse("University", 6.9271, 79.8612));
        response.setDestination(new LocationResponse("Fort", 6.9344, 79.8428));
        response.setRequestedAt(Instant.now());
        response.setCreatedAt(Instant.now());
        response.setUpdatedAt(Instant.now());
        return response;
    }
}
