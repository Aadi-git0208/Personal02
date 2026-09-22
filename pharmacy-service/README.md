# MediCurex Pharmacy Service

Medicine catalog and inventory for the MediCurex SOA platform.

This service owns **medicines and stock only**. It does not store users, passwords, prescriptions, or orders. There is no foreign key to another service database.

Order Service must look up price, validate stock, and update stock through these REST APIs. It must not access `medicurex_pharmacy` directly.

## Prerequisites

- JDK 21+
- Maven 3.9+
- MySQL 8
- Eureka at [http://localhost:8761](http://localhost:8761)

## Database

Schema: `medicurex_pharmacy`

```sql
CREATE DATABASE IF NOT EXISTS medicurex_pharmacy
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Table `medicines` is created by Hibernate `ddl-auto=update`.

## Environment

| Variable | Purpose |
|---|---|
| `PHARMACY_DB_URL` | JDBC URL (defaults to local `medicurex_pharmacy`) |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials (same env names as other services) |
| `JWT_SECRET` | **Same secret as Identity/Auth** (needed for admin writes) |

## How to run

```bash
cd Personal02/eureka-server
mvn spring-boot:run

cd Personal02/pharmacy-service
mvn spring-boot:run
```

API: `http://localhost:8085`  
Gateway: `http://localhost:8080/api/medicines/**`

```bash
mvn test
```

## Port and Eureka

| Item | Value |
|---|---|
| Application name | `medicurex-pharmacy-service` |
| Port | `8085` |
| Health | `http://localhost:8085/actuator/health` |
| Eureka | `MEDICUREX-PHARMACY-SERVICE` |

## Business rules

- Price and stock cannot be negative
- DELETE is a soft delete (`active=false`); the medicine disappears from catalog reads
- Active medicine names must be unique (case-insensitive)
- Catalog reads are public so patients, doctors, Prescription Service, and Order Service can look up medicines
- Only ADMIN can create, update, delete, or change stock

## APIs

| Method | Path | Auth |
|---|---|---|
| `GET /api/medicines` | active catalog | public |
| `GET /api/medicines/search?query=` | search name, description, category | public |
| `GET /api/medicines/{id}` | lookup (name, price, stock) | public |
| `POST /api/medicines` | create | ADMIN |
| `PUT /api/medicines/{id}` | update | ADMIN |
| `DELETE /api/medicines/{id}` | soft delete | ADMIN |
| `PATCH /api/medicines/{id}/stock` | set stock (`stockQuantity`) or adjust (`adjustment`) | ADMIN set; PATIENT/ADMIN adjust (Order Service checkout) |

### Create / update

```json
{
  "name": "Paracetamol",
  "description": "Fever and pain relief",
  "category": "Analgesic",
  "price": 50.00,
  "stockQuantity": 100,
  "imageUrl": "https://example.com/paracetamol.png"
}
```

### Stock update (Order Service)

Set absolute stock:

```json
{ "stockQuantity": 100 }
```

Adjust stock (negative decrement for an order):

```json
{ "adjustment": -2 }
```

Provide exactly one of `stockQuantity` or `adjustment`. An adjustment that would go below zero returns `409`.

`GET /api/medicines/{id}` is the Order Service lookup: `id`, `name`, `price`, and `stockQuantity`.

## Example

```bash
curl http://localhost:8085/api/medicines

curl -X POST http://localhost:8085/api/medicines ^
  -H "Authorization: Bearer <admin-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Paracetamol\",\"price\":50.00,\"stockQuantity\":100,\"category\":\"Analgesic\"}"

curl -X PATCH http://localhost:8085/api/medicines/1/stock ^
  -H "Authorization: Bearer <admin-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"adjustment\":-1}"
```

## Status codes

| Code | When |
|---|---|
| 201/200/204 | Success |
| 400 | Validation failed or both/neither stock fields |
| 401 | Missing/invalid JWT on a write |
| 403 | Non-admin catalog write, or patient setting absolute stock |
| 404 | Medicine missing or soft-deleted |
| 409 | Duplicate active name, or insufficient stock |

## Dependencies

- Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.3
- Spring Web, Security, Data JPA, Validation, Actuator
- Eureka client
- MySQL 8, JJWT (validation only)
