package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.client.dto.FareCalculationRequestDto;
import com.ridelink.ridemanagement.client.dto.FareCalculationResponseDto;

/**
 * Interface defining communication with the Fare & Payment Service.
 * Follows the Dependency Inversion Principle.
 */
public interface FarePaymentServiceClient {

    /**
     * Sends ride completion data to calculate final fare and record simulated payment.
     *
     * @param request completion details including distance and timestamps
     * @return fare calculation breakdown and payment confirmation
     */
    FareCalculationResponseDto processRideCompletion(FareCalculationRequestDto request);
}
