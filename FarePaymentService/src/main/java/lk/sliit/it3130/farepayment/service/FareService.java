package lk.sliit.it3130.farepayment.service;

import lk.sliit.it3130.farepayment.dto.FareEstimateRequest;
import lk.sliit.it3130.farepayment.dto.FinalFareRequest;
import lk.sliit.it3130.farepayment.model.Fare;
import lk.sliit.it3130.farepayment.repository.FareRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FareService {

    // Documented rule:
    // Fare = Base Fare + (Distance x Per-Km Rate) + (Duration x Per-Minute Rate)
    // Base = LKR 150.00, Per Km = LKR 80.00, Per Minute = LKR 10.00
    private static final BigDecimal BASE_FARE = BigDecimal.valueOf(150);
    private static final BigDecimal PER_KM = BigDecimal.valueOf(80);
    private static final BigDecimal PER_MINUTE = BigDecimal.valueOf(10);

    private final FareRepository fareRepository;

    public FareService(FareRepository fareRepository) {
        this.fareRepository = fareRepository;
    }

    public BigDecimal calculate(double distanceKm, int durationMinutes) {
        return BASE_FARE
                .add(PER_KM.multiply(BigDecimal.valueOf(distanceKm)))
                .add(PER_MINUTE.multiply(BigDecimal.valueOf(durationMinutes)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Fare estimate(FareEstimateRequest request) {
        return new Fare(
                UUID.randomUUID(), null, request.distanceKm(), request.durationMinutes(),
                calculate(request.distanceKm(), request.durationMinutes()), "LKR", LocalDateTime.now());
    }

    public Fare createFinalFare(FinalFareRequest request) {
        Fare fare = new Fare(
                UUID.randomUUID(), request.rideId(), request.distanceKm(), request.durationMinutes(),
                calculate(request.distanceKm(), request.durationMinutes()), "LKR", LocalDateTime.now());
        return fareRepository.save(fare);
    }

    public Fare getLatestForRide(UUID rideId) {
        return fareRepository.findTopByRideIdOrderByCreatedAtDesc(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Fare not found for ride: " + rideId));
    }
}
