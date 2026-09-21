# MediCurex Authentication Service

Authentication and authorization foundation for the MediCurex healthcare platform.

This backend currently implements **only** user registration, login, JWT authentication, and role-based authorization. Patient, doctor, appointment, pharmacy, chat, and admin management features are intentionally out of scope.

## Stack

- Java 21 (compiles on JDK 21+)
- Spring Boot 3.5
- Spring Security
- Spring Data JPA
- MySQL 8
- JWT (JJWT)
- BCrypt password hashing
- Maven

## 1. Project setup

Requirements:

- JDK 21 or newer
- Maven 3.9+
- MySQL 8 running locally

From this folder:

```bash
cd Personal02/Backend
```

## 2. MySQL database setup

Create the `apexcare` database if it does not already exist:

```sql
CREATE DATABASE IF NOT EXISTS apexcare
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

The JDBC URL also includes `createDatabaseIfNotExist=true`, so Hibernate can create the database if the MySQL user has permission.

On first startup, JPA `ddl-auto=update` creates the `users` table:

```sql
CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
);
```

`role` is stored as lowercase: `patient`, `doctor`, or `admin`.

Passwords are stored only as BCrypt hashes.

## 3. Environment variables

Copy `.env.example` and export the values, or set them in your shell / IDE run configuration.

| Variable | Purpose | Example |
|---|---|---|
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/apexcare?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | your local password |
| `JWT_SECRET` | Signing key, **at least 32 characters** | a long random string |
| `JWT_EXPIRATION` | Token lifetime in milliseconds | `86400000` (24 hours) |
| `ADMIN_NAME` | Seeded admin name | `MediCurex Admin` |
| `ADMIN_EMAIL` | Seeded admin email | `admin@medicurex.local` |
| `ADMIN_PASSWORD` | Seeded admin password | a strong password |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:5173` |
| `ADMIN_SEED` | Create the initial admin if none exists | `true` |

Do **not** commit real secrets. Local development defaults exist in `application.properties` only so the project can start; change them before any real deployment.

Windows PowerShell example:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-mysql-password"
$env:JWT_SECRET="replace-with-a-long-random-secret-key-at-least-32-chars"
$env:JWT_EXPIRATION="86400000"
$env:ADMIN_NAME="MediCurex Admin"
$env:ADMIN_EMAIL="admin@medicurex.local"
$env:ADMIN_PASSWORD="ChangeMeAdmin123!"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173"
```

## 4. How to run

```bash
mvn spring-boot:run
```

Or:

```bash
mvn clean package
java -jar target/medicurex-auth-0.0.1-SNAPSHOT.jar
```

The API starts at `http://localhost:8080`.

Run tests:

```bash
mvn test
```

Tests use an in-memory H2 database so they do not require MySQL.

## 5. How JWT authentication works

1. A patient or doctor registers with `POST /api/auth/register`.
2. The password is hashed with BCrypt and stored in MySQL.
3. The user logs in with `POST /api/auth/login`.
4. The server verifies the password and returns a signed JWT.
5. The client sends the token on later requests:

   ```
   Authorization: Bearer <token>
   ```

6. `JwtAuthenticationFilter` reads the header, verifies the signature, checks expiration, loads the user, and sets the Spring Security context.
7. Roles come from the server-side user / validated JWT claims. The frontend cannot assign itself a role after login.

JWT claims:

```json
{
  "sub": "rahul@gmail.com",
  "userId": 1,
  "role": "patient",
  "iat": 1710000000,
  "exp": 1710086400
}
```

Sessions are stateless (`SessionCreationPolicy.STATELESS`). CSRF is disabled because this is a JWT REST API.

## 6. Registration API

`POST /api/auth/register` — public

Allowed roles: `patient`, `doctor`.

`admin` is rejected. The first admin is created by the seeder, not by this endpoint.

Request:

```json
{
  "name": "Rahul",
  "email": "rahul@gmail.com",
  "password": "rahul123",
  "role": "patient"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "name": "Rahul",
  "email": "rahul@gmail.com",
  "role": "patient"
}
```

Errors:

- `400` validation failure or admin registration attempt
- `409` email already registered

## 7. Login API

`POST /api/auth/login` — public

Request:

```json
{
  "email": "rahul@gmail.com",
  "password": "rahul123"
}
```

Response `200 OK`:

```json
{
  "token": "<JWT>",
  "type": "Bearer",
  "user": {
    "id": 1,
    "name": "Rahul",
    "email": "rahul@gmail.com",
    "role": "patient"
  }
}
```

Invalid email or password returns `401 Unauthorized` with the same message, so callers cannot probe which emails exist.

## 8. Current user API

`GET /api/auth/me` — requires JWT

Header:

```
Authorization: Bearer <JWT>
```

Response `200 OK`:

```json
{
  "id": 1,
  "name": "Rahul",
  "email": "rahul@gmail.com",
  "role": "patient"
}
```

Missing or invalid token returns `401`.

## 9. Role-based authorization

Spring Security authorities:

- `ROLE_PATIENT`
- `ROLE_DOCTOR`
- `ROLE_ADMIN`

Temporary test endpoints:

| Endpoint | PATIENT | DOCTOR | ADMIN |
|---|---|---|---|
| `GET /api/patient/test` | 200 | 403 | 403 |
| `GET /api/doctor/test` | 403 | 200 | 403 |
| `GET /api/admin/test` | 403 | 403 | 200 |

Later controllers can use:

```java
@PreAuthorize("hasRole('PATIENT')")
@PreAuthorize("hasRole('DOCTOR')")
@PreAuthorize("hasRole('ADMIN')")
```

## 10. How the initial admin is created

Public registration cannot create an `admin`.

On startup, `AdminSeeder` runs if `app.admin.seed=true` (default):

1. If any admin already exists, it does nothing.
2. Otherwise it creates one admin from `ADMIN_NAME`, `ADMIN_EMAIL`, and `ADMIN_PASSWORD`.
3. The password is BCrypt hashed before insert.

There is no unauthenticated endpoint that creates admins.

## 11. Example Postman requests

Base URL: `http://localhost:8080`

### Register a patient

`POST /api/auth/register`

```json
{
  "name": "Rahul",
  "email": "rahul@gmail.com",
  "password": "rahul123",
  "role": "patient"
}
```

### Register a doctor

`POST /api/auth/register`

```json
{
  "name": "Dr Sharma",
  "email": "doctor@gmail.com",
  "password": "doctor123",
  "role": "doctor"
}
```

### Rejected admin registration

`POST /api/auth/register`

```json
{
  "name": "Admin",
  "email": "admin@gmail.com",
  "password": "admin123",
  "role": "admin"
}
```

Expected: `400 Bad Request`.

### Login

`POST /api/auth/login`

```json
{
  "email": "rahul@gmail.com",
  "password": "rahul123"
}
```

Copy `token` from the response.

### Current user

`GET /api/auth/me`

Header: `Authorization: Bearer {{token}}`

### Role checks

```
GET /api/patient/test
GET /api/doctor/test
GET /api/admin/test
```

Use the same `Authorization` header. A patient token should succeed only on `/api/patient/test`.

### Admin login

Use the seeded admin credentials, then call `GET /api/admin/test`.

## Error format

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "timestamp": "2026-09-21T04:00:00Z"
}
```

Validation errors also include `fieldErrors`.

## Security rules

- Passwords are never stored, returned, or logged in plaintext
- JWT secrets are configuration, not source-code constants for production
- Public registration cannot create admins
- Authenticated roles come from the server, not from the frontend
- Endpoints are authenticated by default except register and login
- CORS allows only configured frontend origins
