package lk.sliit.it3130.farepayment.service;

import lk.sliit.it3130.farepayment.dto.PaymentRequest;
import lk.sliit.it3130.farepayment.model.Payment;
import lk.sliit.it3130.farepayment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment record(PaymentRequest request) {
        if (paymentRepository.findByRideId(request.rideId()).isPresent()) {
            throw new IllegalStateException("A payment already exists for this ride.");
        }

        String method = request.method().trim().toUpperCase();
        if (!method.equals("CASH") && !method.equals("CARD") && !method.equals("WALLET")) {
            throw new IllegalArgumentException("Payment method must be CASH, CARD, or WALLET.");
        }

        // Simulated payment: no real gateway is contacted.
        Payment payment = new Payment(
                UUID.randomUUID(),
                request.rideId(),
                request.amount().setScale(2),
                "LKR",
                method,
                "PAID",
                "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }

    public Payment getByRide(UUID rideId) {
        return paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for ride: " + rideId));
    }
}
