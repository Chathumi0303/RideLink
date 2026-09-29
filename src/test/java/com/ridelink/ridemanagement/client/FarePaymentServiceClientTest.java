package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.client.dto.FareCalculationRequestDto;
import com.ridelink.ridemanagement.client.dto.FareCalculationResponseDto;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("FarePaymentServiceClient REST Integration Unit Tests")
class FarePaymentServiceClientTest {

    private RestClient restClient;
    private MockRestServiceServer mockServer;
    private FarePaymentServiceClient client;

    private final String baseUrl = "http://localhost:8084";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        client = new FarePaymentServiceClientImpl(restClient);
    }

    @Test
    @DisplayName("Successfully processes fare calculation and simulated payment")
    void testProcessRideCompletionSuccess() {
        String jsonResponse = """
                {
                    "rideId": "ride-123",
                    "baseFare": 100.00,
                    "distanceFare": 350.00,
                    "timeFare": 50.00,
                    "totalFare": 500.00,
                    "paymentStatus": "COMPLETED",
                    "transactionId": "TXN-887766"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8084/api/fares/complete-ride"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        FareCalculationRequestDto request = new FareCalculationRequestDto(
                "ride-123", "passenger-1", "driver-1", 7.5, 20L,
                Instant.now().minusSeconds(1200), Instant.now()
        );

        FareCalculationResponseDto response = client.processRideCompletion(request);

        assertNotNull(response);
        assertEquals("ride-123", response.getRideId());
        assertEquals(0, new BigDecimal("500.00").compareTo(response.getTotalFare()));
        assertEquals("COMPLETED", response.getPaymentStatus());
        mockServer.verify();
    }

    @Test
    @DisplayName("Throws ExternalServiceException on 500 server failure from payment service")
    void testProcessRideCompletionServerErrorThrows() {
        mockServer.expect(requestTo("http://localhost:8084/api/fares/complete-ride"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        FareCalculationRequestDto request = new FareCalculationRequestDto(
                "ride-123", "passenger-1", "driver-1", 7.5, 20L,
                Instant.now().minusSeconds(1200), Instant.now()
        );

        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () ->
                client.processRideCompletion(request));

        assertEquals("Fare & Payment Service", ex.getServiceName());
        mockServer.verify();
    }
}
