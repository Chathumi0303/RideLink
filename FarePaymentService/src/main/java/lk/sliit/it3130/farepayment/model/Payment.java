package lk.sliit.it3130.farepayment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "payments")
public class Payment {

    @Id
    private UUID id;

    private UUID rideId;

    private BigDecimal amount;

    private String currency;

    private String method;

    private String status;

    private String reference;

    private LocalDateTime createdAt;

    public Payment() {}

    public Payment(UUID id, UUID rideId, BigDecimal amount, String currency,
                   String method, String status, String reference, LocalDateTime createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.status = status;
        this.reference = reference;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRideId() {
        return rideId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }

    public String getReference() {
        return reference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}