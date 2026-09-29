package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.client.dto.DriverResponseDto;
import com.ridelink.ridemanagement.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@DisplayName("DriverServiceClient REST Integration Unit Tests")
class DriverServiceClientTest {

    private RestClient restClient;
    private MockRestServiceServer mockServer;
    private DriverServiceClient driverServiceClient;

    private final String baseUrl = "http://localhost:8082";

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
        driverServiceClient = new DriverServiceClientImpl(restClient);
    }

    @Test
    @DisplayName("Successfully retrieves eligible available drivers")
    void testGetEligibleDriversSuccess() {
        String jsonResponse = """
                [
                    {
                        "driverId": "driver-1",
                        "driverName": "Kamal Perera",
                        "vehicleNumber": "CAB-1122",
                        "vehicleType": "SEDAN",
                        "status": "AVAILABLE",
                        "currentLatitude": 6.9275,
                        "currentLongitude": 79.8615,
                        "rating": 4.9,
                        "distanceKm": 0.8
                    }
                ]
                """;

        mockServer.expect(requestTo("http://localhost:8082/api/drivers/available?latitude=6.9271&longitude=79.8612"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<DriverResponseDto> drivers = driverServiceClient.getEligibleAvailableDrivers(6.9271, 79.8612);

        assertNotNull(drivers);
        assertEquals(1, drivers.size());
        assertEquals("driver-1", drivers.get(0).getDriverId());
        assertEquals("Kamal Perera", drivers.get(0).getDriverName());
        mockServer.verify();
    }

    @Test
    @DisplayName("Handles 404 by returning empty list gracefully")
    void testGetEligibleDrivers404ReturnsEmptyList() {
        mockServer.expect(requestTo("http://localhost:8082/api/drivers/available?latitude=6.9271&longitude=79.8612"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        List<DriverResponseDto> drivers = driverServiceClient.getEligibleAvailableDrivers(6.9271, 79.8612);

        assertNotNull(drivers);
        assertTrue(drivers.isEmpty());
        mockServer.verify();
    }

    @Test
    @DisplayName("Throws ExternalServiceException when service returns 500 error")
    void testGetEligibleDrivers500ThrowsExternalServiceException() {
        mockServer.expect(requestTo("http://localhost:8082/api/drivers/available?latitude=6.9271&longitude=79.8612"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        ExternalServiceException ex = assertThrows(ExternalServiceException.class, () ->
                driverServiceClient.getEligibleAvailableDrivers(6.9271, 79.8612));

        assertEquals("Driver & Vehicle Service", ex.getServiceName());
        mockServer.verify();
    }
}
