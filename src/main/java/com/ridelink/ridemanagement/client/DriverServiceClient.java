package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.client.dto.DriverResponseDto;

import java.util.List;

/**
 * Interface defining communication with the Driver & Vehicle Service.
 * Follows the Dependency Inversion Principle to isolate business logic from HTTP mechanics.
 */
public interface DriverServiceClient {

    /**
     * Retrieves eligible and currently available drivers within proximity of the pickup location.
     *
     * @param pickupLatitude  latitude of pickup point
     * @param pickupLongitude longitude of pickup point
     * @return list of eligible available drivers
     */
    List<DriverResponseDto> getEligibleAvailableDrivers(double pickupLatitude, double pickupLongitude);
}
