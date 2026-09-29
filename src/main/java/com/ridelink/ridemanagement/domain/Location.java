package com.ridelink.ridemanagement.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object representing a geographical location in the RideLink domain.
 */
public class Location implements Serializable {

    private String placeName;
    private Double latitude;
    private Double longitude;

    public Location() {
    }

    public Location(String placeName, Double latitude, Double longitude) {
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Calculates the great-circle distance between two locations using the Haversine formula.
     *
     * @param other target location
     * @return distance in kilometers rounded to two decimal places
     */
    public double distanceTo(Location other) {
        if (other == null || this.latitude == null || this.longitude == null ||
                other.latitude == null || other.longitude == null) {
            return 0.0;
        }

        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_KM * c;
        return Math.round(distance * 100.0) / 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(placeName, location.placeName) &&
                Objects.equals(latitude, location.latitude) &&
                Objects.equals(longitude, location.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeName, latitude, longitude);
    }

    @Override
    public String toString() {
        return "Location{" +
                "placeName='" + placeName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
