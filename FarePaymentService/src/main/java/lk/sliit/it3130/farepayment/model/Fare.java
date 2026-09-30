package lk.sliit.it3130.farepayment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "fares")
public class Fare {

    @Id
    private UUID id;

    private UUID rideId;

    private double distanceKm;

    private int durationMinutes;

    private BigDecimal amount;

    private String currency;

    private LocalDateTime createdAt;

    public Fare() {}

    public Fare(UUID id, UUID rideId, double distanceKm, int durationMinutes,
                BigDecimal amount, String currency, LocalDateTime createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRideId() {
        return rideId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}