# MediCurex Appointment Service

Appointment lifecycle service for the MediCurex SOA platform.

This service owns **appointments only**. It does not store users, passwords, doctor profiles, prescriptions, medicines, or chat. `patientId` and stored `doctorId` are Identity Service user ids. There is no foreign key to another service database.

Doctor details and `consultationFee` are loaded over REST from **Profile Service**.

## Prerequisites

- JDK 21+
- Maven 3.9+
- MySQL 8
- Eureka at [http://localhost:8761](http://localhost:8761)
- Profile Service registered as `medicurex-profile-service` (needed when booking)

## Database

Schema: `medicurex_appointment`

```sql
CREATE DATABASE IF NOT EXISTS medicurex_appointment
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Table `appointments` is created by Hibernate `ddl-auto=update`.

## Environment

| Variable | Purpose |
|---|---|
| `APPOINTMENT_DB_URL` | JDBC URL (defaults to local `medicurex_appointment`) |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials (same env names as other services) |
| `JWT_SECRET` | **Same secret as Identity/Auth** |
| `PROFILE_SERVICE_URL` | Eureka name, default `http://medicurex-profile-service` |

## How to run

```bash
cd Personal02/eureka-server
mvn spring-boot:run

cd Personal02/profile-service
mvn spring-boot:run

cd Personal02/appointment-service
mvn spring-boot:run
```

API: `http://localhost:8082`  
Gateway: `http://localhost:8080/api/appointments/**`

```bash
mvn test
```

## Port and Eureka

| Item | Value |
|---|---|
| Application name | `medicurex-appointment-service` |
| Port | `8082` |
| Health | `http://localhost:8082/actuator/health` |
| Eureka | `MEDICUREX-APPOINTMENT-SERVICE` |

## Status values

`PENDING` → `CONFIRMED` or `REJECTED` or `CANCELLED`  
`CONFIRMED` → `COMPLETED` or `CANCELLED`

## APIs

All routes require `Authorization: Bearer <JWT>`.

Create request `doctorId` is the **Profile Service doctor profile id** from `GET /api/profiles/doctors`. Stored `doctorId` is the doctor's Identity `userId`. `patientId` always comes from the JWT; a body `patientId` is ignored.

| Method | Path | Role |
|---|---|---|
| `POST /api/appointments` | create | PATIENT |
| `GET /api/appointments/patient/me` | own list | PATIENT |
| `GET /api/appointments/doctor/me` | own list | DOCTOR |
| `GET /api/appointments` | all | ADMIN |
| `GET /api/appointments/{id}` | one | owner patient, assigned doctor, or ADMIN |
| `PATCH /api/appointments/{id}/cancel` | cancel | PATIENT (own, pending/confirmed) |
| `PATCH /api/appointments/{id}/accept` | accept | DOCTOR (own, pending) |
| `PATCH /api/appointments/{id}/reject` | reject | DOCTOR (own, pending) |
| `PATCH /api/appointments/{id}/complete` | complete | DOCTOR (own, confirmed) |

### Create

```json
{
  "doctorId": 1,
  "appointmentDate": "2026-09-25",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "reason": "Follow-up"
}
```

Rules:

- patient cannot book themselves as doctor
- pending/confirmed slots for the same doctor cannot overlap
- `consultationFee` is copied from Profile Service
- startTime must be before endTime

## Example

```bash
curl -X POST http://localhost:8082/api/appointments ^
  -H "Authorization: Bearer <patient-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"doctorId\":1,\"appointmentDate\":\"2026-09-25\",\"startTime\":\"10:00:00\",\"endTime\":\"10:30:00\"}"

curl http://localhost:8082/api/appointments/doctor/me ^
  -H "Authorization: Bearer <doctor-token>"

curl -X PATCH http://localhost:8082/api/appointments/1/accept ^
  -H "Authorization: Bearer <doctor-token>"
```

## Status codes

| Code | When |
|---|---|
| 201/200 | Success |
| 400 | Validation or self-booking |
| 401 | Missing/invalid JWT |
| 403 | Wrong owner/role |
| 404 | Appointment or doctor profile not found |
| 409 | Overlap or illegal status transition |
| 502 | Profile Service unavailable |

## Dependencies

- Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.3
- Spring Web, Security, Data JPA, Validation, Actuator
- Eureka client + LoadBalancer
- MySQL 8, JJWT (validation only)
