# MediCurex Chat Service

Secure patient-doctor messaging for the MediCurex SOA platform.

This service owns **conversations and messages only**. It does not store users, passwords, appointments, or doctor profiles. `patientId`, `doctorId`, and `senderId` are Identity Service user ids. There is no foreign key to another service database.

A conversation is opened only after Appointment Service confirms a valid patient-doctor appointment over REST.

## Prerequisites

- JDK 21+
- Maven 3.9+
- MySQL 8
- Eureka at [http://localhost:8761](http://localhost:8761)
- Appointment Service registered as `medicurex-appointment-service` (needed when opening a conversation)

## Database

Schema: `medicurex_chat`

```sql
CREATE DATABASE IF NOT EXISTS medicurex_chat
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Tables `conversations` and `messages` are created by Hibernate `ddl-auto=update`.

One conversation is stored per `appointmentId`.

## Environment

| Variable | Purpose |
|---|---|
| `CHAT_DB_URL` | JDBC URL (defaults to local `medicurex_chat`) |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials (same env names as other services) |
| `JWT_SECRET` | **Same secret as Identity/Auth** |
| `APPOINTMENT_SERVICE_URL` | Eureka name, default `http://medicurex-appointment-service` |

## How to run

```bash
cd Personal02/eureka-server
mvn spring-boot:run

cd Personal02/appointment-service
mvn spring-boot:run

cd Personal02/chat-service
mvn spring-boot:run
```

API: `http://localhost:8083`  
Gateway: `http://localhost:8080/api/chat/**`

```bash
mvn test
```

## Port and Eureka

| Item | Value |
|---|---|
| Application name | `medicurex-chat-service` |
| Port | `8083` |
| Health | `http://localhost:8083/actuator/health` |
| Eureka | `MEDICUREX-CHAT-SERVICE` |

## Relationship check

`POST /api/chat/conversations` calls `GET /api/appointments/{id}` on Appointment Service and forwards the caller JWT.

Chat is allowed when the appointment status is `PENDING`, `CONFIRMED`, or `COMPLETED`. `REJECTED` and `CANCELLED` appointments are rejected with `409`.

Stored `patientId` and `doctorId` are copied from the appointment (Identity user ids). The request body only supplies `appointmentId`.

## Access rules

- Only the conversation patient or doctor can read or write that conversation
- A patient cannot open or read another patient's conversation
- A doctor cannot open or read another doctor's conversation
- ADMIN can list and read conversations/messages, but cannot create conversations, send messages, or mark messages read

## APIs

All routes require `Authorization: Bearer <JWT>`.

| Method | Path | Role |
|---|---|---|
| `POST /api/chat/conversations` | open or reopen by appointment | PATIENT or DOCTOR (participant) |
| `GET /api/chat/conversations` | own list (ADMIN: all) | PATIENT, DOCTOR, ADMIN |
| `GET /api/chat/conversations/{id}` | one conversation | participant or ADMIN |
| `GET /api/chat/conversations/{id}/messages` | message history | participant or ADMIN |
| `POST /api/chat/conversations/{id}/messages` | send message | PATIENT or DOCTOR (participant) |
| `PATCH /api/chat/messages/{id}/read` | mark read | PATIENT or DOCTOR (recipient only) |

### Create conversation

```json
{
  "appointmentId": 1
}
```

Re-posting the same appointment returns the existing conversation.

### Send message

```json
{
  "content": "Hello doctor"
}
```

`senderId` always comes from the JWT. Max length is 2000 characters.

## Realtime later

Messages are persisted over REST today. `ChatRealtimeNotifier` is a no-op hook so a later WebSocket/STOMP publisher can emit the same `MessageResponse` without changing the service layer. Do not add Kafka or RabbitMQ yet.

## Example

```bash
curl -X POST http://localhost:8083/api/chat/conversations ^
  -H "Authorization: Bearer <patient-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"appointmentId\":1}"

curl http://localhost:8083/api/chat/conversations/1/messages ^
  -H "Authorization: Bearer <doctor-token>"

curl -X POST http://localhost:8083/api/chat/conversations/1/messages ^
  -H "Authorization: Bearer <patient-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"content\":\"Hello doctor\"}"

curl -X PATCH http://localhost:8083/api/chat/messages/1/read ^
  -H "Authorization: Bearer <doctor-token>"
```

## Status codes

| Code | When |
|---|---|
| 201/200 | Success |
| 400 | Validation failed |
| 401 | Missing/invalid JWT |
| 403 | Wrong owner/role, admin write, or sender marking own message |
| 404 | Conversation, message, or appointment not found |
| 409 | Appointment is not eligible for chat |
| 502 | Appointment Service unavailable |

## Dependencies

- Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.3
- Spring Web, Security, Data JPA, Validation, Actuator
- Eureka client + LoadBalancer
- MySQL 8, JJWT (validation only)
