package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.client.dto.DriverResponseDto;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.List;

/**
 * REST HTTP Client adapter for Driver & Vehicle Service.
 */
@Component
public class DriverServiceClientImpl implements DriverServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceClientImpl.class);

    private final RestClient restClient;

    public DriverServiceClientImpl(@Qualifier("driverServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<DriverResponseDto> getEligibleAvailableDrivers(double pickupLatitude, double pickupLongitude) {
        log.info("Requesting eligible available drivers near lat={}, lon={}", pickupLatitude, pickupLongitude);

        try {
            List<DriverResponseDto> drivers = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/drivers/available")
                            .queryParam("latitude", pickupLatitude)
                            .queryParam("longitude", pickupLongitude)
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.warn("Driver & Vehicle Service returned client error status: {}", response.getStatusCode());
                        if (response.getStatusCode().value() == 404) {
                            // If endpoint or resource returns 404, treat as no drivers available
                            return;
                        }
                        throw new ExternalServiceException("Driver & Vehicle Service",
                                response.getStatusCode().value(), "Failed to retrieve available drivers");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Driver & Vehicle Service returned server error: {}", response.getStatusCode());
                        throw new ExternalServiceException("Driver & Vehicle Service",
                                response.getStatusCode().value(), "Driver service encountered an internal error");
                    })
                    .body(new ParameterizedTypeReference<List<DriverResponseDto>>() {});

            if (drivers == null) {
                return Collections.emptyList();
            }

            log.info("Driver & Vehicle Service returned {} eligible driver(s)", drivers.size());
            return drivers;

        } catch (ResourceAccessException ex) {
            log.error("Network timeout or connection refused contacting Driver & Vehicle Service: {}", ex.getMessage());
            throw new ExternalServiceException("Driver & Vehicle Service",
                    "Service is currently unavailable or timed out", ex);
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error contacting Driver & Vehicle Service", ex);
            throw new ExternalServiceException("Driver & Vehicle Service",
                    "Unexpected error during driver retrieval: " + ex.getMessage(), ex);
        }
    }
}
