package com.ridelink.ridemanagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Geographic location response")
public class LocationResponse {

    @Schema(description = "Name or address of location", example = "University Entrance, Colombo 03")
    private String placeName;

    @Schema(description = "Latitude coordinate", example = "6.9271")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "79.8612")
    private Double longitude;

    public LocationResponse() {
    }

    public LocationResponse(String placeName, Double latitude, Double longitude) {
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
}
