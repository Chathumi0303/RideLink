package com.ridelink.ridemanagement.mapper;

import com.ridelink.ridemanagement.domain.Location;
import com.ridelink.ridemanagement.domain.Ride;
import com.ridelink.ridemanagement.dto.request.LocationRequest;
import com.ridelink.ridemanagement.dto.response.LocationResponse;
import com.ridelink.ridemanagement.dto.response.RideResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper component for bidirectional domain and DTO transformations.
 */
@Component
public class RideMapper {

    public Location toLocation(LocationRequest request) {
        if (request == null) {
            return null;
        }
        return new Location(request.getPlaceName(), request.getLatitude(), request.getLongitude());
    }

    public LocationResponse toLocationResponse(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationResponse(location.getPlaceName(), location.getLatitude(), location.getLongitude());
    }

    public RideResponse toRideResponse(Ride ride) {
        if (ride == null) {
            return null;
        }
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setPassengerId(ride.getPassengerId());
        response.setDriverId(ride.getDriverId());
        response.setPickup(toLocationResponse(ride.getPickup()));
        response.setDestination(toLocationResponse(ride.getDestination()));
        response.setStatus(ride.getStatus());
        response.setRequestedAt(ride.getRequestedAt());
        response.setAssignedAt(ride.getAssignedAt());
        response.setAcceptedAt(ride.getAcceptedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        response.setCancelledAt(ride.getCancelledAt());
        response.setCancellationReason(ride.getCancellationReason());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setFinalFare(ride.getFinalFare());
        response.setDistanceKm(ride.getDistanceKm());
        response.setNotes(ride.getNotes());
        response.setCreatedAt(ride.getCreatedAt());
        response.setUpdatedAt(ride.getUpdatedAt());
        return response;
    }
}
