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
import com.ridelink.ridemanagement.dto.response.RideResponse;
import com.ridelink.ridemanagement.exception.InvalidRideStateException;
import com.ridelink.ridemanagement.exception.NoDriverAvailableException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.exception.UnauthorizedRideAccessException;
import com.ridelink.ridemanagement.mapper.RideMapper;
import com.ridelink.ridemanagement.repository.RideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Production-grade implementation of RideService orchestrating domain aggregates,
 * repository persistence, and external microservice communication.
 */
@Service
public class RideServiceImpl implements RideService {

    private static final Logger log = LoggerFactory.getLogger(RideServiceImpl.class);

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FarePaymentServiceClient farePaymentServiceClient;
    private final RideMapper rideMapper;

    public RideServiceImpl(
            RideRepository rideRepository,
            DriverServiceClient driverServiceClient,
            FarePaymentServiceClient farePaymentServiceClient,
            RideMapper rideMapper) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.farePaymentServiceClient = farePaymentServiceClient;
        this.rideMapper = rideMapper;
    }

    @Override
    public RideResponse createRide(CreateRideRequest request, String authenticatedPassengerId) {
        log.info("Creating new ride request for passengerId: {}", authenticatedPassengerId);

        Location pickup = rideMapper.toLocation(request.getPickup());
        Location destination = rideMapper.toLocation(request.getDestination());

        Ride ride = Ride.createNewRequest(
                authenticatedPassengerId,
                pickup,
                destination,
                request.getNotes(),
                request.getEstimatedFare(),
                request.getDistanceKm()
        );

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride created successfully with id: {} and status: {}", savedRide.getId(), savedRide.getStatus());
        return rideMapper.toRideResponse(savedRide);
    }

    @Override
    public RideResponse getRideById(String rideId, String authenticatedUserId, boolean isAdmin) {
        log.debug("Fetching ride id: {} for user: {}, isAdmin: {}", rideId, authenticatedUserId, isAdmin);
        Ride ride = findRideOrThrow(rideId);

        boolean isPassenger = Objects.equals(ride.getPassengerId(), authenticatedUserId);
        boolean isDriver = Objects.equals(ride.getDriverId(), authenticatedUserId);

        if (!isAdmin && !isPassenger && !isDriver) {
            log.warn("Access denied to ride id: {} for user: {}", rideId, authenticatedUserId);
            throw new UnauthorizedRideAccessException(
                    String.format("You are not authorized to view ride '%s'", rideId)
            );
        }

        return rideMapper.toRideResponse(ride);
    }

    @Override
    public List<RideResponse> getRidesByPassengerId(String passengerId, String authenticatedUserId, boolean isAdmin) {
        if (!isAdmin && !Objects.equals(passengerId, authenticatedUserId)) {
            log.warn("User {} attempted to view passenger rides of {}", authenticatedUserId, passengerId);
            throw new UnauthorizedRideAccessException("You are not authorized to view another passenger's rides");
        }

        List<Ride> rides = rideRepository.findByPassengerIdOrderByRequestedAtDesc(passengerId);
        return rides.stream().map(rideMapper::toRideResponse).toList();
    }

    @Override
    public List<RideResponse> getRidesByDriverId(String driverId, String authenticatedUserId, boolean isAdmin) {
        if (!isAdmin && !Objects.equals(driverId, authenticatedUserId)) {
            log.warn("User {} attempted to view driver rides of {}", authenticatedUserId, driverId);
            throw new UnauthorizedRideAccessException("You are not authorized to view another driver's rides");
        }

        List<Ride> rides = rideRepository.findByDriverIdOrderByRequestedAtDesc(driverId);
        return rides.stream().map(rideMapper::toRideResponse).toList();
    }

    @Override
    public List<RideResponse> getMyRides(String authenticatedUserId, boolean isDriver, boolean isPassenger) {
        if (isDriver) {
            return getRidesByDriverId(authenticatedUserId, authenticatedUserId, false);
        }
        return getRidesByPassengerId(authenticatedUserId, authenticatedUserId, false);
    }

    @Override
    public RideResponse assignDriver(String rideId, String authenticatedUserId, boolean isAdmin) {
        log.info("Attempting driver assignment for ride id: {}", rideId);
        Ride ride = findRideOrThrow(rideId);

        // Verify authorization: only the requesting passenger or admin can trigger assignment
        if (!isAdmin && !Objects.equals(ride.getPassengerId(), authenticatedUserId)) {
            throw new UnauthorizedRideAccessException(
                    String.format("User '%s' is not authorized to request driver assignment for ride '%s'",
                            authenticatedUserId, rideId)
            );
        }

        // Verify current status allows assignment
        if (!ride.getStatus().canTransitionTo(RideStatus.ASSIGNED)) {
            throw new InvalidRideStateException(
                    String.format("Ride '%s' cannot be assigned because its current status is %s", rideId, ride.getStatus())
            );
        }

        // External Call: Obtain eligible available drivers from Driver & Vehicle Service
        double pickupLat = ride.getPickup().getLatitude();
        double pickupLon = ride.getPickup().getLongitude();

        List<DriverResponseDto> eligibleDrivers = driverServiceClient.getEligibleAvailableDrivers(pickupLat, pickupLon);

        if (eligibleDrivers == null || eligibleDrivers.isEmpty()) {
            log.warn("No available drivers returned by Driver & Vehicle Service for ride {}", rideId);
            throw new NoDriverAvailableException("No eligible drivers available in the pickup area. Please retry shortly.");
        }

        // Deterministic Driver Selection Algorithm:
        // Prioritize driver with shortest distance if distance is provided; otherwise select the first eligible driver
        DriverResponseDto selectedDriver = eligibleDrivers.stream()
                .filter(d -> d.getDriverId() != null && !d.getDriverId().isBlank())
                .min(Comparator.comparing(d -> d.getDistanceKm() != null ? d.getDistanceKm() : 0.0))
                .orElse(eligibleDrivers.get(0));

        log.info("Assigned driver '{}' (Vehicle: {}) to ride '{}'",
                selectedDriver.getDriverId(), selectedDriver.getVehicleNumber(), rideId);

        // Safe transition: updates status to ASSIGNED and records assignedAt
        ride.assignDriver(selectedDriver.getDriverId());
        Ride saved = rideRepository.save(ride);

        return rideMapper.toRideResponse(saved);
    }

    @Override
    public RideResponse acceptRide(String rideId, String authenticatedDriverId) {
        log.info("Driver '{}' attempting to accept ride '{}'", authenticatedDriverId, rideId);
        Ride ride = findRideOrThrow(rideId);

        // Domain method validates status == ASSIGNED and driverId match
        ride.accept(authenticatedDriverId);
        Ride saved = rideRepository.save(ride);

        log.info("Ride '{}' accepted by driver '{}'", rideId, authenticatedDriverId);
        return rideMapper.toRideResponse(saved);
    }

    @Override
    public RideResponse startRide(String rideId, String authenticatedDriverId) {
        log.info("Driver '{}' starting ride '{}'", authenticatedDriverId, rideId);
        Ride ride = findRideOrThrow(rideId);

        // Domain method validates status == ACCEPTED and driverId match
        ride.start(authenticatedDriverId);
        Ride saved = rideRepository.save(ride);

        log.info("Ride '{}' started and is now IN_PROGRESS", rideId);
        return rideMapper.toRideResponse(saved);
    }

    @Override
    public RideResponse completeRide(String rideId, String authenticatedDriverId) {
        log.info("Driver '{}' completing ride '{}'", authenticatedDriverId, rideId);
        Ride ride = findRideOrThrow(rideId);

        // Validate driver authorization and status prior to external call
        if (!Objects.equals(ride.getDriverId(), authenticatedDriverId)) {
            throw new UnauthorizedRideAccessException(
                    String.format("Driver '%s' is not authorized to complete ride '%s'", authenticatedDriverId, rideId)
            );
        }

        if (!ride.getStatus().canTransitionTo(RideStatus.COMPLETED)) {
            throw new InvalidRideStateException(
                    String.format("Cannot complete ride '%s' because its current status is %s", rideId, ride.getStatus())
            );
        }

        Instant now = Instant.now();
        Instant startTime = ride.getStartedAt() != null ? ride.getStartedAt() : ride.getRequestedAt();
        long durationMinutes = Math.max(1, Duration.between(startTime, now).toMinutes());

        // Prepare integration payload for Fare & Payment Service
        FareCalculationRequestDto fareRequest = new FareCalculationRequestDto(
                ride.getId(),
                ride.getPassengerId(),
                ride.getDriverId(),
                ride.getDistanceKm(),
                durationMinutes,
                ride.getStartedAt(),
                now
        );

        // External Call: calculate fare and process payment
        FareCalculationResponseDto fareResponse = farePaymentServiceClient.processRideCompletion(fareRequest);

        BigDecimal finalFare = fareResponse.getTotalFare() != null ?
                fareResponse.getTotalFare() : (ride.getEstimatedFare() != null ? ride.getEstimatedFare() : BigDecimal.ZERO);

        // Complete aggregate state transition
        ride.complete(authenticatedDriverId, finalFare);
        Ride saved = rideRepository.save(ride);

        log.info("Ride '{}' successfully completed with final fare {}", rideId, finalFare);
        return rideMapper.toRideResponse(saved);
    }

    @Override
    public RideResponse cancelRide(String rideId, String authenticatedUserId, boolean isAdmin, CancelRideRequest request) {
        log.info("User '{}' cancelling ride '{}'", authenticatedUserId, rideId);
        Ride ride = findRideOrThrow(rideId);

        // Domain method checks cancellation rules and authorization
        ride.cancel(authenticatedUserId, isAdmin, request.getReason());
        Ride saved = rideRepository.save(ride);

        log.info("Ride '{}' cancelled. Reason: {}", rideId, request.getReason());
        return rideMapper.toRideResponse(saved);
    }

    private Ride findRideOrThrow(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(String.format("Ride not found with id: '%s'", rideId)));
    }
}
