package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.dto.request.CancelRideRequest;
import com.ridelink.ridemanagement.dto.request.CreateRideRequest;
import com.ridelink.ridemanagement.dto.response.RideResponse;

import java.util.List;

/**
 * Service interface encapsulating Ride business operations and lifecycle workflows.
 */
public interface RideService {

    RideResponse createRide(CreateRideRequest request, String authenticatedPassengerId);

    RideResponse getRideById(String rideId, String authenticatedUserId, boolean isAdmin);

    List<RideResponse> getRidesByPassengerId(String passengerId, String authenticatedUserId, boolean isAdmin);

    List<RideResponse> getRidesByDriverId(String driverId, String authenticatedUserId, boolean isAdmin);

    List<RideResponse> getMyRides(String authenticatedUserId, boolean isDriver, boolean isPassenger);

    RideResponse assignDriver(String rideId, String authenticatedUserId, boolean isAdmin);

    RideResponse acceptRide(String rideId, String authenticatedDriverId);

    RideResponse startRide(String rideId, String authenticatedDriverId);

    RideResponse completeRide(String rideId, String authenticatedDriverId);

    RideResponse cancelRide(String rideId, String authenticatedUserId, boolean isAdmin, CancelRideRequest request);
}
