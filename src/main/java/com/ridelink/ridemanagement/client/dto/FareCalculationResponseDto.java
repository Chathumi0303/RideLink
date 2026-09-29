package com.ridelink.ridemanagement.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Response received from Fare & Payment Service with calculated fare breakdown.
 */
public class FareCalculationResponseDto implements Serializable {

    private String rideId;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal timeFare;
    private BigDecimal totalFare;
    private String paymentStatus;
    private String transactionId;

    public FareCalculationResponseDto() {
    }

    public FareCalculationResponseDto(String rideId, BigDecimal baseFare, BigDecimal distanceFare,
                                      BigDecimal timeFare, BigDecimal totalFare,
                                      String paymentStatus, String transactionId) {
        this.rideId = rideId;
        this.baseFare = baseFare;
        this.distanceFare = distanceFare;
        this.timeFare = timeFare;
        this.totalFare = totalFare;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getDistanceFare() {
        return distanceFare;
    }

    public void setDistanceFare(BigDecimal distanceFare) {
        this.distanceFare = distanceFare;
    }

    public BigDecimal getTimeFare() {
        return timeFare;
    }

    public void setTimeFare(BigDecimal timeFare) {
        this.timeFare = timeFare;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
