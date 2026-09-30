# RideLink – Fare & Payment Service

IT3130 Application Development Group Assignment – Member 4 service.

## Responsibility
This service owns:
- Fare estimation
- Final fare calculation
- Simulated payment recording
- Payment status
- Receipt/payment retrieval

The assignment brief explicitly defines Fare & Payment as the fourth core microservice and requires independent persistence ownership. See the supplied brief.

## Technology
- Java 21
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- H2 database (service-owned)
- Spring Security
- JWT verification
- Swagger/OpenAPI
- Maven

## Fare rule
`Fare = 150 + (distanceKm × 80) + (durationMinutes × 10)`

Currency: LKR.

Example:
5 km + 10 minutes = 150 + 400 + 100 = LKR 650.00.

## Run
Prerequisites:
- JDK 21
- Maven 3.9+

From this folder:

```bash
mvn clean test
mvn spring-boot:run
```

Service URL:
`http://localhost:8084`

Swagger:
`http://localhost:8084/swagger-ui.html`

H2 console:
`http://localhost:8084/h2-console`

JDBC URL:
`jdbc:h2:file:./data/farepaymentdb`

## JWT integration
The service expects:

`Authorization: Bearer <JWT>`

The JWT must be signed using the same `JWT_SECRET` used by the Account Service and contain:
- `sub`
- `roles` array, for example `["PASSENGER"]`

Set the secret before running:

Windows PowerShell:
```powershell
$env:JWT_SECRET="your-shared-secret-at-least-32-characters"
mvn spring-boot:run
```

Do not commit the real secret.

## API endpoints

### 1. Estimate fare
`POST /api/fares/estimate`

```json
{
  "distanceKm": 5,
  "durationMinutes": 10
}
```

### 2. Save final fare
`POST /api/fares/final`

```json
{
  "rideId": "11111111-1111-1111-1111-111111111111",
  "distanceKm": 5,
  "durationMinutes": 10
}
```

### 3. Get ride fare
`GET /api/fares/ride/{rideId}`

### 4. Record simulated payment
`POST /api/payments`

```json
{
  "rideId": "11111111-1111-1111-1111-111111111111",
  "amount": 650.00,
  "method": "CARD"
}
```

Allowed methods: CASH, CARD, WALLET.

### 5. Retrieve payment/receipt
`GET /api/payments/ride/{rideId}`

## Negative test cases
1. Invalid distance: `distanceKm = 0` -> HTTP 400.
2. Unsupported payment method -> HTTP 400/validation/business error.
3. Duplicate payment for same ride -> HTTP 409.
4. Missing/invalid JWT -> HTTP 401.

## Interservice integration
Ride Management should call this service through REST when it needs a fare estimate/final fare. The Ride ID is the stable identifier. This service never queries Ride Management's database.

The group should document the final API contract agreed by all four members and demonstrate at least two meaningful interservice interactions in the integrated system.

## Important group integration note
If the Account Service uses a different JWT claim name or signing algorithm, update `JwtAuthenticationFilter` to match the group's agreed contract rather than bypassing authentication.

## Git contribution
Recommended branch:
`feature/<student-id>/fare-payment-service`

Suggested commits:
1. `feat: initialize fare payment spring boot service`
2. `feat: implement fare calculation and persistence`
3. `feat: implement simulated payment and receipt retrieval`
4. `feat: add jwt role authorization`
5. `test: add fare calculation unit tests`
6. `docs: add fare payment api documentation`

Use a pull request and peer review before merging.

## Academic integrity
The assignment brief states that submitted work must be understood by the responsible member and that permitted generative-AI use should be declared according to institute policy. Review, test, adapt and explain every part before submission.
# RideLink
RideLink Backend Microservices - Group Assignment
