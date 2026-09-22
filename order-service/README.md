# MediCurex Order Service

Patient carts and pharmacy orders for the MediCurex SOA platform.

This service owns **carts and orders only**. It does not store users, passwords, or the medicine catalog. `patientId` is the Identity Service user id. There is no foreign key to another service database.

Medicine existence, current price, stock checks, and inventory updates go through **Pharmacy Service over REST**.

## Prerequisites

- JDK 21+
- Maven 3.9+
- MySQL 8
- Eureka at [http://localhost:8761](http://localhost:8761)
- Pharmacy Service registered as `medicurex-pharmacy-service`

## Database

Schema: `medicurex_order`

```sql
CREATE DATABASE IF NOT EXISTS medicurex_order
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Tables `carts`, `cart_items`, `orders`, and `order_items` are created by Hibernate `ddl-auto=update`.

One cart is stored per patient.

## Environment

| Variable | Purpose |
|---|---|
| `ORDER_DB_URL` | JDBC URL (defaults to local `medicurex_order`) |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials (same env names as other services) |
| `JWT_SECRET` | **Same secret as Identity/Auth** |
| `PHARMACY_SERVICE_URL` | Eureka name, default `http://medicurex-pharmacy-service` |

## How to run

```bash
cd Personal02/eureka-server
mvn spring-boot:run

cd Personal02/pharmacy-service
mvn spring-boot:run

cd Personal02/order-service
mvn spring-boot:run
```

API: `http://localhost:8086`  
Gateway: `http://localhost:8080/api/orders/**`

```bash
mvn test
```

## Port and Eureka

| Item | Value |
|---|---|
| Application name | `medicurex-order-service` |
| Port | `8086` |
| Health | `http://localhost:8086/actuator/health` |
| Eureka | `MEDICUREX-ORDER-SERVICE` |

## Business rules

- Only PATIENT can manage a cart or checkout
- Doctors cannot place pharmacy orders
- Patients see only their own orders
- ADMIN can list orders and change status
- Frontend `price` / `totalAmount` are ignored
- Checkout loads current price and stock from Pharmacy Service
- `OrderItem.priceSnapshot` and `subtotal` are stored at checkout
- Ordering more than available stock returns `409`
- If a stock decrement fails after earlier items succeeded, those decrements are restored and no order is kept
- Admin `CANCELLED` restores stock through Pharmacy Service

## Order status

`PLACED` → `CONFIRMED` or `CANCELLED`  
`CONFIRMED` → `PACKED` or `CANCELLED`  
`PACKED` → `SHIPPED` or `CANCELLED`  
`SHIPPED` → `DELIVERED`

## APIs

All routes require `Authorization: Bearer <JWT>`.

| Method | Path | Role |
|---|---|---|
| `GET /api/orders/cart` | get or create cart | PATIENT |
| `POST /api/orders/cart/items` | add / merge quantity | PATIENT |
| `PUT /api/orders/cart/items/{medicineId}` | set quantity | PATIENT |
| `DELETE /api/orders/cart/items/{medicineId}` | remove line | PATIENT |
| `DELETE /api/orders/cart` | empty cart | PATIENT |
| `POST /api/orders/checkout` | place order | PATIENT |
| `GET /api/orders/my-orders` | own orders | PATIENT |
| `GET /api/orders` | all orders | ADMIN |
| `GET /api/orders/{id}` | one order | owner patient or ADMIN |
| `PATCH /api/orders/{id}/status` | `{ "status": "CONFIRMED" }` | ADMIN |

### Add to cart

```json
{
  "medicineId": 2,
  "quantity": 2
}
```

### Checkout

No body. Totals are calculated from Pharmacy Service prices.

## Example

```bash
curl http://localhost:8086/api/orders/cart ^
  -H "Authorization: Bearer <patient-token>"

curl -X POST http://localhost:8086/api/orders/cart/items ^
  -H "Authorization: Bearer <patient-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"medicineId\":2,\"quantity\":2}"

curl -X POST http://localhost:8086/api/orders/checkout ^
  -H "Authorization: Bearer <patient-token>"

curl -X PATCH http://localhost:8086/api/orders/1/status ^
  -H "Authorization: Bearer <admin-token>" ^
  -H "Content-Type: application/json" ^
  -d "{\"status\":\"CONFIRMED\"}"
```

## Status codes

| Code | When |
|---|---|
| 201/200 | Success |
| 400 | Empty cart or validation failed |
| 401 | Missing/invalid JWT |
| 403 | Doctor/admin on cart, or another patient's order |
| 404 | Cart item, order, or medicine not found |
| 409 | Insufficient stock or illegal status change |
| 502 | Pharmacy Service unavailable |

## Dependencies

- Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.3
- Spring Web, Security, Data JPA, Validation, Actuator
- Eureka client + LoadBalancer
- MySQL 8, JJWT (validation only)
