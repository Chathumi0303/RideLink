# Fare & Payment workflow

Passenger -> Ride Management
1. Passenger requests a ride.
2. Ride Management has/obtains distance and duration.
3. Ride Management calls Fare & Payment `/api/fares/estimate`.
4. Fare & Payment returns estimated LKR amount.
5. Ride completes.
6. Ride Management calls `/api/fares/final` with rideId and actual distance/duration.
7. Passenger/payment flow calls `/api/payments`.
8. Fare & Payment creates a simulated payment record with PAID status and reference.
9. Client retrieves `/api/payments/ride/{rideId}` as the receipt/payment record.

Failure examples:
- Invalid fare input -> 400.
- Duplicate payment -> 409.
- Missing/invalid token -> 401.
- Insufficient role -> 403.
