package lk.sliit.it3130.farepayment.controller;

import jakarta.validation.Valid;
import lk.sliit.it3130.farepayment.dto.FareEstimateRequest;
import lk.sliit.it3130.farepayment.dto.FinalFareRequest;
import lk.sliit.it3130.farepayment.model.Fare;
import lk.sliit.it3130.farepayment.service.FareService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.UUID;

@RestController
@RequestMapping("/api/fares")
@SecurityRequirement(name = "bearerAuth")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public Fare estimate(@Valid @RequestBody FareEstimateRequest request) {
        return fareService.estimate(request);
    }

    @PostMapping("/final")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public Fare finalFare(@Valid @RequestBody FinalFareRequest request) {
        return fareService.createFinalFare(request);
    }

    @GetMapping("/ride/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public Fare getRideFare(@PathVariable UUID rideId) {
        return fareService.getLatestForRide(rideId);
    }
}
