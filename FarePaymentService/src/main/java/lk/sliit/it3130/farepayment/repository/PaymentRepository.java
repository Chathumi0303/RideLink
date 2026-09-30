package lk.sliit.it3130.farepayment.repository;

import lk.sliit.it3130.farepayment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends MongoRepository<Payment, UUID> {

    Optional<Payment> findByRideId(UUID rideId);
}