# MediCurex Prescription Service

Doctor-written prescriptions for the MediCurex SOA platform.

This service owns **prescriptions and prescription items only**. It does not store users, passwords, appointments, or the medicine catalog. `patientId` and `doctorId` are Identity Service user ids. There is no foreign key to another service database.

Appointment ownership and status are checked over REST with **Appointment Service**. When an item includes `medicineId`, the catalog name is loaded over REST from **Pharmacy Service**.

## Prerequisites

- JDK 21+
- Maven 3.9+
- MySQL 8
- Eureka at [http://localhost:8761](http://localhost:8761)
- Appointment Service registered as `medicurex-appointment-service` (needed when creating)
- Pharmacy Service registered as `medicurex-pharmacy-service` only when items include `medicineId`

## Database

Schema: `medicurex_prescription`

```sql
CREATE DATABASE IF NOT EXISTS medicurex_prescription
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Tables `prescriptions` and `prescription_items` are created by Hibernate `ddl-auto=update`.

One prescription is stored per `appointmentId`.

## Environment

| Variable | Purpose |
|---|---|
| `PRESCRIPTION_DB_URL` | JDBC URL (defaults to local `medicurex_prescription`) |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials (same env names as other services) |
| `JWT_SECRET` | **Same secret as Identity/Auth** |
| `APPOINTMENT_SERVICE_URL` | Eureka name, default `http://medicurex-appointment-service` |
| `PHARMACY_SERVICE_URL` | Eureka name, default `http://medicurex-pharmacy-service` |

## How to run

```bash
cd Personal02/eureka-server
mvn spring-boot:run

cd Personal02/appointment-service
mvn spring-boot:run

cd Personal02/prescription-service
mvn spring-boot:run
```

API: `http://localhost:8084`  
Gateway: `http://localhost:8080/api/prescriptions/**`

```bash
mvn test
```

## Port and Eureka

| Item | Value |
|---|---|
| Application name | `medicurex-prescription-service` |
| Port | `8084` |
| Health | `http://localhost:8084/actuator/health` |
| Eureka | `MEDICUREX-PRESCRIPTION-SERVICE` |

## Business rules

- Only a DOCTOR can create a prescription
- The JWT doctor must own the appointment (`appointment.doctorId`)
- Appointment status must be `CONFIRMED` or `COMPLETED`
- `patientId` and `doctorId` are copied from Appointment Service; body identity fields are ignored
- One prescription per appointment
- Each item requires dosage, frequency, and duration
- `medicineId` is optional; if present, Pharmacy Service `GET /api/medicines/{id}` supplies the stored name
- Without `medicineId`, `medicineName` is required
- Patients see only their prescriptions
- Doctors see only prescriptions they wrote
- ADMIN can list and read all

## APIs

All routes require `Authorization: Bearer <JWT>`.

| Method | Path | Role |
|---|---|---|
| `POST /api/prescriptions` | create | DOCTOR |
| `GET /api/prescriptions/doctor/me` | own list | DOCTOR |
| `GET /api/prescriptions/patient/me` | own list | PATIENT |
| `GET /api/prescriptions` | all | ADMIN |
| `GET /api/prescriptions/{id}` | one | owner patient, writing doctor, or ADMIN |

### Create

```json
{
  "appointmentId": 1,
  "diagnosis": "Viral fever",
  "notes": "Rest and fluids",
  "items": [
    {
      "medicineId": 9,
      "medicineName": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Twice daily",
      "duration": "5 days",
      "instructions": "After food"
    }
  ]
}
```

## Example

```bash
curl -X POST http://localhost:8084/api/prescriptions ^
  -H "Authorization: Bearer <doctor-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"appointmentId\":1,\"diagnosis\":\"Viral fever\",\"items\":[{\"medicineName\":\"Paracetamol\",\"dosage\":\"500mg\",\"frequency\":\"Twice daily\",\"duration\":\"5 days\"}]}"

curl http://localhost:8084/api/prescriptions/doctor/me ^
  -H "Authorization: Bearer <doctor-token>"

curl http://localhost:8084/api/prescriptions/patient/me ^
  -H "Authorization: Bearer <patient-token>"
```

## Status codes

| Code | When |
|---|---|
| 201/200 | Success |
| 400 | Validation failed or missing medicine name |
| 401 | Missing/invalid JWT |
| 403 | Wrong owner/role |
| 404 | Prescription, appointment, or medicine not found |
| 409 | Appointment not eligible, or prescription already exists |
| 502 | Appointment or Pharmacy Service unavailable |

## Dependencies

- Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.3
- Spring Web, Security, Data JPA, Validation, Actuator
- Eureka client + LoadBalancer
- MySQL 8, JJWT (validation only)
