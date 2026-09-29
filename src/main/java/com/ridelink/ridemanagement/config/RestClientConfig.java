package com.ridelink.ridemanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuration for HTTP REST communication with peer microservices.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient driverServiceRestClient(
            @Value("${services.driver-vehicle.base-url}") String baseUrl,
            @Value("${services.driver-vehicle.connect-timeout-ms:3000}") int connectTimeout,
            @Value("${services.driver-vehicle.read-timeout-ms:5000}") int readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RestClient farePaymentServiceRestClient(
            @Value("${services.fare-payment.base-url}") String baseUrl,
            @Value("${services.fare-payment.connect-timeout-ms:3000}") int connectTimeout,
            @Value("${services.fare-payment.read-timeout-ms:5000}") int readTimeout) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeout));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
