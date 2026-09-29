package com.ridelink.ridemanagement.repository;

import com.ridelink.ridemanagement.domain.Ride;
import com.ridelink.ridemanagement.domain.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the 'rides' collection.
 * Isolates persistence logic cleanly within the Ride Management Service boundary.
 */
@Repository
public interface RideRepository extends MongoRepository<Ride, String> {

    List<Ride> findByPassengerIdOrderByRequestedAtDesc(String passengerId);

    List<Ride> findByDriverIdOrderByRequestedAtDesc(String driverId);

    List<Ride> findByStatus(RideStatus status);

    List<Ride> findByPassengerIdAndStatus(String passengerId, RideStatus status);

    List<Ride> findByDriverIdAndStatus(String driverId, RideStatus status);

    boolean existsByIdAndStatus(String id, RideStatus status);
}
