# MediCurex Profile Service

Healthcare profile service for the MediCurex SOA platform.

This service owns **patient profiles**, **doctor profiles**, and **doctor availability**. It does not own passwords, login, or JWT issuance. Identity remains in `medicurex-auth`. `userId` is the Identity Service user id stored as a plain value — there is no `User` entity and no foreign key to the auth database.

## Prerequisites

- JDK 21 or newer
- Maven 3.9+
- MySQL 8
- Eureka Server at [http://localhost:8761](http://localhost:8761)

## Database

Schema: `medicurex_profile` (created automatically if the MySQL user can create databases).

```sql
CREATE DATABASE IF NOT EXISTS medicurex_profile
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Tables (created by Hibernate `ddl-auto=update`):

- `patient_profiles` — unique `user_id`
- `doctor_profiles` — unique `user_id`
- `doctor_availability` — `doctor_id` references `doctor_profiles.id`

This service never reads `apexcare.users`.

## Environment

| Variable | Purpose | Example |
|---|---|---|
| `PROFILE_DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/medicurex_profile?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | your local password |
| `JWT_SECRET` | **Same signing secret as Identity/Auth**, at least 32 characters | shared secret |

Use the same `JWT_SECRET` as `medicurex-auth` so tokens issued at login can be validated here.

## How to run

Start Eureka first, then this service:

```bash
cd Personal02/eureka-server
mvn spring-boot:run

cd Personal02/profile-service
mvn spring-boot:run
```

Or:

```bash
cd Personal02/profile-service
mvn clean package
java -jar target/medicurex-profile-service-0.0.1-SNAPSHOT.jar
```

The API listens on `http://localhost:8081`.

Through the API Gateway (`http://localhost:8080`) the same paths are available at `/api/profiles/**`.

Run tests (H2, Eureka disabled):

```bash
mvn test
```

## Port and Eureka

| Item | Value |
|---|---|
| Application name | `medicurex-profile-service` |
| Port | `8081` |
| Health | `http://localhost:8081/actuator/health` |
| Eureka | registers as `MEDICUREX-PROFILE-SERVICE` |

Gateway route: `/api/profiles/**` → `lb://medicurex-profile-service`.

## Security

All profile APIs require `Authorization: Bearer <JWT>` from Identity/Auth.

`/me` endpoints take the authenticated `userId` from the JWT. A `userId` in the request body is ignored.

| Path | PATIENT | DOCTOR | ADMIN |
|---|---|---|---|
| `GET/PUT /api/profiles/patient/me` | own profile | 403 | 403 |
| `GET/PUT /api/profiles/doctor/me` | 403 | own profile | 403 |
| doctor availability `/me/availability` | 403 | own slots | 403 |
| `GET /api/profiles/doctors` | yes | yes | yes |
| `GET /api/profiles/doctors/{doctorId}` | yes | yes | yes |

Directory endpoints return only **completed** doctor profiles. Admin can view the directory; identity/user accounts stay in the Auth service.

## APIs

### Patient profile

`GET /api/profiles/patient/me` — JWT role `patient`

`PUT /api/profiles/patient/me` — upsert own profile

```json
{
  "fullName": "Rahul",
  "phone": "9999999999",
  "gender": "male",
  "dateOfBirth": "1998-04-12",
  "address": "Pune"
}
```

`profileCompleted` is set to `true` when full name, phone, gender, and date of birth are present.

### Doctor profile

`GET /api/profiles/doctor/me` — JWT role `doctor`

`PUT /api/profiles/doctor/me` — upsert own profile

```json
{
  "fullName": "Dr Sharma",
  "phone": "8888888888",
  "specialization": "Cardiology",
  "experience": 8,
  "consultationFee": 500,
  "profileImage": "https://example.com/doc.jpg"
}
```

Rules:

- `consultationFee` and `experience` cannot be negative
- a completed profile requires full name, specialization, experience, and consultation fee
- specialization cannot be blank when the profile is completed

### Doctor availability

`GET    /api/profiles/doctor/me/availability`
`POST   /api/profiles/doctor/me/availability`
`PUT    /api/profiles/doctor/me/availability/{id}`
`DELETE /api/profiles/doctor/me/availability/{id}`

```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "10:00:00",
  "endTime": "12:00:00",
  "available": true
}
```

`dayOfWeek` is a Java `DayOfWeek` (`MONDAY` … `SUNDAY`). `startTime` must be before `endTime`. Overlapping slots on the same day return `409`.

### Doctor directory

`GET /api/profiles/doctors`
`GET /api/profiles/doctors?specialization=Cardiology`
`GET /api/profiles/doctors/{doctorId}`

`doctorId` is the profile id, not the Identity `userId`. Responses include both `id` and `userId`.

## Example requests

Replace `<token>` with a JWT from `POST /api/auth/login`.

```bash
curl http://localhost:8081/actuator/health

curl http://localhost:8081/api/profiles/patient/me ^
  -H "Authorization: Bearer <token>"

curl -X PUT http://localhost:8081/api/profiles/doctor/me ^
  -H "Authorization: Bearer <token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"fullName\":\"Dr Sharma\",\"specialization\":\"Cardiology\",\"experience\":8,\"consultationFee\":500}"

curl http://localhost:8081/api/profiles/doctors ^
  -H "Authorization: Bearer <token>"
```

## Status codes

| Code | When |
|---|---|
| 200/201/204 | Success |
| 400 | Validation failure |
| 401 | Missing or invalid JWT |
| 403 | Wrong role |
| 404 | Profile or availability not found |
| 409 | Overlapping availability |

## Dependencies

- Java 21
- Spring Boot 3.5.6
- Spring Cloud 2025.0.3
- Spring Web, Security, Data JPA, Validation, Actuator
- MySQL 8
- JJWT (validation only)
- Eureka Discovery Client
