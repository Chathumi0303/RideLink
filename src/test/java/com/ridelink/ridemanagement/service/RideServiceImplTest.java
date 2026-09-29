package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.client.DriverServiceClient;
import com.ridelink.ridemanagement.client.FarePaymentServiceClient;
import com.ridelink.ridemanagement.client.dto.DriverResponseDto;
import com.ridelink.ridemanagement.client.dto.FareCalculationRequestDto;
import com.ridelink.ridemanagement.client.dto.FareCalculationResponseDto;
import com.ridelink.ridemanagement.domain.Location;
import com.ridelink.ridemanagement.domain.Ride;
import com.ridelink.ridemanagement.domain.RideStatus;
import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.request.LocationRequest;
import com.ridelink.ridemanagement.dto.response.RideResponse;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.exception.NoDriverAvailableException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.exception.UnauthorizedRideAccessException;
import com.ridelink.ridemanagement.mapper.RideMapper;
import com.ridelink.ridemanagement.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RideServiceImpl Business Logic Unit Tests")
class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private FarePaymentServiceClient farePaymentServiceClient;

    private RideMapper rideMapper;
    private RideService rideService;

    private final String passengerId = "passenger-101";
    private final String driverId = "driver-202";

    @BeforeEach
    void setUp() {
        rideMapper = new RideMapper();
        rideService = new RideServiceImpl(rideRepository, driverServiceClient, farePaymentServiceClient, rideMapper);
    }

    @Test
    @DisplayName("Create ride saves new ride with REQUESTED status")
    void testCreateRideSuccess() {
        CreateRideRequest request = new CreateRideRequest(
                new LocationRequest("Origin", 6.9271, 79.8612),
                new LocationRequest("Destination", 6.9344, 79.8428),
                "Handle luggage carefully"
        );

        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.createRide(request, passengerId);

        assertNotNull(response);
        assertEquals(passengerId, response.getPassengerId());
        assertEquals(RideStatus.REQUESTED, response.getStatus());
        assertEquals("Origin", response.getPickup().getPlaceName());
        assertEquals("Destination", response.getDestination().getPlaceName());
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    @DisplayName("Get ride by ID returns ride for passenger owner")
    void testGetRideByIdPassengerOwner() {
        Ride ride = createTestRide();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        RideResponse response = rideService.getRideById(ride.getId(), passengerId, false);

        assertNotNull(response);
        assertEquals(ride.getId(), response.getId());
    }

    @Test
    @DisplayName("Get ride by ID throws Unauthorized for non-owner stranger")
    void testGetRideByIdStrangerThrowsUnauthorized() {
        Ride ride = createTestRide();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        assertThrows(UnauthorizedRideAccessException.class, () ->
                rideService.getRideById(ride.getId(), "stranger-user", false));
    }

    @Test
    @DisplayName("Get ride by ID allows ADMIN regardless of ownership")
    void testGetRideByIdAdminAllowed() {
        Ride ride = createTestRide();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        RideResponse response = rideService.getRideById(ride.getId(), "admin-user", true);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Get non-existent ride throws RideNotFoundException")
    void testGetRideNotFound() {
        when(rideRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () ->
                rideService.getRideById("missing-id", passengerId, false));
    }

    @Test
    @DisplayName("Assign driver calls DriverServiceClient and updates status to ASSIGNED")
    void testAssignDriverSuccess() {
        Ride ride = createTestRide();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        DriverResponseDto driver1 = new DriverResponseDto(driverId, "John Doe", "CAB-1234", "SEDAN",
                "AVAILABLE", 6.9280, 79.8620, 4.8, 1.2);
        when(driverServiceClient.getEligibleAvailableDrivers(anyDouble(), anyDouble()))
                .thenReturn(List.of(driver1));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.assignDriver(ride.getId(), passengerId, false);

        assertEquals(RideStatus.ASSIGNED, response.getStatus());
        assertEquals(driverId, response.getDriverId());
        assertNotNull(response.getAssignedAt());
        verify(driverServiceClient).getEligibleAvailableDrivers(anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Assign driver throws InvalidRideStateException when ride is already ASSIGNED")
    void testAssignAlreadyAssignedRideThrowsException() {
        Ride ride = createTestRide();
        ride.assignDriver(driverId); // State is now ASSIGNED
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        assertThrows(InvalidRideStateException.class, () ->
                rideService.assignDriver(ride.getId(), passengerId, false));

        verify(driverServiceClient, never()).getEligibleAvailableDrivers(anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Assign driver throws NoDriverAvailableException when no drivers available")
    void testAssignDriverNoDriversAvailable() {
        Ride ride = createTestRide();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(driverServiceClient.getEligibleAvailableDrivers(anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());

        assertThrows(NoDriverAvailableException.class, () ->
                rideService.assignDriver(ride.getId(), passengerId, false));

        // Ensure ride was NOT saved with a corrupted state
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("Assign driver fails gracefully when DriverServiceClient throws ExternalServiceException")
    void testAssignDriverExternalServiceFailure() {
        Ride ride = createTestRide();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(driverServiceClient.getEligibleAvailableDrivers(anyDouble(), anyDouble()))
                .thenThrow(new ExternalServiceException("Driver & Vehicle Service", "Service timeout"));

        assertThrows(ExternalServiceException.class, () ->
                rideService.assignDriver(ride.getId(), passengerId, false));

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("Accept ride transitions status to ACCEPTED")
    void testAcceptRideSuccess() {
        Ride ride = createTestRide();
        ride.assignDriver(driverId);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.acceptRide(ride.getId(), driverId);

        assertEquals(RideStatus.ACCEPTED, response.getStatus());
        assertNotNull(response.getAcceptedAt());
    }

    @Test
    @DisplayName("Start ride transitions status to IN_PROGRESS")
    void testStartRideSuccess() {
        Ride ride = createTestRide();
        ride.assignDriver(driverId);
        ride.accept(driverId);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.startRide(ride.getId(), driverId);

        assertEquals(RideStatus.IN_PROGRESS, response.getStatus());
        assertNotNull(response.getStartedAt());
    }

    @Test
    @DisplayName("Complete ride calls Fare & Payment Service and transitions to COMPLETED")
    void testCompleteRideSuccess() {
        Ride ride = createTestRide();
        ride.assignDriver(driverId);
        ride.accept(driverId);
        ride.start(driverId);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        FareCalculationResponseDto fareResponse = new FareCalculationResponseDto(
                ride.getId(), BigDecimal.valueOf(100.0), BigDecimal.valueOf(300.0),
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(450.0), "SUCCESS", "TX-999"
        );
        when(farePaymentServiceClient.processRideCompletion(any(FareCalculationRequestDto.class)))
                .thenReturn(fareResponse);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.completeRide(ride.getId(), driverId);

        assertEquals(RideStatus.COMPLETED, response.getStatus());
        assertEquals(BigDecimal.valueOf(450.0), response.getFinalFare());
        assertNotNull(response.getCompletedAt());
        verify(farePaymentServiceClient).processRideCompletion(any(FareCalculationRequestDto.class));
    }

    @Test
    @DisplayName("Complete ride does not update state if FarePaymentService throws exception")
    void testCompleteRidePaymentFailureDoesNotCorruptState() {
        Ride ride = createTestRide();
        ride.assignDriver(driverId);
        ride.accept(driverId);
        ride.start(driverId);
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));

        when(farePaymentServiceClient.processRideCompletion(any(FareCalculationRequestDto.class)))
                .thenThrow(new ExternalServiceException("Fare & Payment Service", "Payment gateway connection timeout"));

        assertThrows(ExternalServiceException.class, () ->
                rideService.completeRide(ride.getId(), driverId));

        // Ensure state remains IN_PROGRESS and was not saved as COMPLETED
        assertEquals(RideStatus.IN_PROGRESS, ride.getStatus());
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("Cancel ride transitions status to CANCELLED")
    void testCancelRideSuccess() {
        Ride ride = createTestRide();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CancelRideRequest request = new CancelRideRequest("Passenger emergency");
        RideResponse response = rideService.cancelRide(ride.getId(), passengerId, false, request);

        assertEquals(RideStatus.CANCELLED, response.getStatus());
        assertEquals("Passenger emergency", response.getCancellationReason());
        assertNotNull(response.getCancelledAt());
    }

    private Ride createTestRide() {
        Location pickup = new Location("Pickup Spot", 6.9271, 79.8612);
        Location destination = new Location("Drop Spot", 6.9344, 79.8428);
        return Ride.createNewRequest(passengerId, pickup, destination, "Notes",
                BigDecimal.valueOf(400.0), 4.5);
    }
}
