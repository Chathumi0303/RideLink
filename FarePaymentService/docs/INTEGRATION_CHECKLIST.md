# Integration checklist for the four-member group

1. Account Service:
   - Agree JWT signing algorithm and secret/environment variable.
   - Agree role claim format, e.g. `roles: ["PASSENGER"]`.

2. Ride Management Service:
   - Call `POST /api/fares/estimate` before/while showing a ride estimate.
   - Call `POST /api/fares/final` after ride completion using the stable `rideId`.
   - Call `POST /api/payments` to record the simulated payment.
   - Retrieve receipt with `GET /api/payments/ride/{rideId}`.

3. Driver & Vehicle Service:
   - No direct database access to this service.
   - Use service APIs only.

4. Database:
   - This service owns the H2 data store.
   - Other services must not read `fares` or `payments` tables directly.

5. Final group evidence:
   - Show at least two meaningful interservice interactions.
   - Add all endpoints to the shared Postman collection.
   - Update architecture and sequence diagrams.
   - Run unit tests for all four services.
