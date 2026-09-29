package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.client.dto.FareCalculationRequestDto;
import com.ridelink.ridemanagement.client.dto.FareCalculationResponseDto;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

/**
 * REST HTTP Client adapter for Fare & Payment Service.
 */
@Component
public class FarePaymentServiceClientImpl implements FarePaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FarePaymentServiceClientImpl.class);

    private final RestClient restClient;

    public FarePaymentServiceClientImpl(@Qualifier("farePaymentServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public FareCalculationResponseDto processRideCompletion(FareCalculationRequestDto request) {
        log.info("Sending ride completion fare calculation request for rideId={}, distanceKm={}",
                request.getRideId(), request.getDistanceKm());

        try {
            FareCalculationResponseDto response = restClient.post()
                    .uri("/api/fares/complete-ride")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        log.warn("Fare & Payment Service returned client error status: {}", res.getStatusCode());
                        throw new ExternalServiceException("Fare & Payment Service",
                                res.getStatusCode().value(), "Fare calculation request rejected by service");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Fare & Payment Service returned server error: {}", res.getStatusCode());
                        throw new ExternalServiceException("Fare & Payment Service",
                                res.getStatusCode().value(), "Fare & Payment service encountered internal failure");
                    })
                    .body(FareCalculationResponseDto.class);

            if (response == null) {
                throw new ExternalServiceException("Fare & Payment Service", "Empty response received from service");
            }

            log.info("Fare calculation completed successfully for rideId={}, totalFare={}",
                    request.getRideId(), response.getTotalFare());
            return response;

        } catch (ResourceAccessException ex) {
            log.error("Network timeout or connection refused contacting Fare & Payment Service: {}", ex.getMessage());
            throw new ExternalServiceException("Fare & Payment Service",
                    "Service is currently unavailable or timed out", ex);
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error contacting Fare & Payment Service", ex);
            throw new ExternalServiceException("Fare & Payment Service",
                    "Unexpected error during fare calculation: " + ex.getMessage(), ex);
        }
    }
}
