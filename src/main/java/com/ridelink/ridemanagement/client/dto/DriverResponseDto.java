package com.ridelink.ridemanagement.client.dto;

import java.io.Serializable;

/**
 * DTO representing an eligible available driver returned by the Driver & Vehicle Service.
 */
public class DriverResponseDto implements Serializable {

    private String driverId;
    private String driverName;
    private String vehicleNumber;
    private String vehicleType;
    private String status;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double rating;
    private Double distanceKm;

    public DriverResponseDto() {
    }

    public DriverResponseDto(String driverId, String driverName, String vehicleNumber,
                             String vehicleType, String status, Double currentLatitude,
                             Double currentLongitude, Double rating, Double distanceKm) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.status = status;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.rating = rating;
        this.distanceKm = distanceKm;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    @Override
    public String toString() {
        return "DriverResponseDto{" +
                "driverId='" + driverId + '\'' +
                ", driverName='" + driverName + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", status='" + status + '\'' +
                ", rating=" + rating +
                ", distanceKm=" + distanceKm +
                '}';
    }
}
