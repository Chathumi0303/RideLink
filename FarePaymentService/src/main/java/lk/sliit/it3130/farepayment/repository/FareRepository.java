package lk.sliit.it3130.farepayment.repository;

import lk.sliit.it3130.farepayment.model.Fare;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface FareRepository extends MongoRepository<Fare, UUID> {

    Optional<Fare> findTopByRideIdOrderByCreatedAtDesc(UUID rideId);
}