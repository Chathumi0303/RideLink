package lk.sliit.it3130.farepayment.controller;

import jakarta.validation.Valid;
import lk.sliit.it3130.farepayment.dto.PaymentRequest;
import lk.sliit.it3130.farepayment.model.Payment;
import lk.sliit.it3130.farepayment.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER','ADMIN')")
    public Payment pay(@Valid @RequestBody PaymentRequest request) {
        return paymentService.record(request);
    }

    @GetMapping("/ride/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public Payment getPayment(@PathVariable UUID rideId) {
        return paymentService.getByRide(rideId);
    }
}
