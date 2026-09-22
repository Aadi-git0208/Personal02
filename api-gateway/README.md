# MediCurex API Gateway

Public HTTP entry point for the MediCurex SOA platform.

This application is a standalone Spring Cloud Gateway. It performs routing, CORS, and Eureka registration only. It has no business logic, no JWT issuance, and no database. Authentication stays in the Identity/Auth Service (`medicurex-auth`).

## Prerequisites

- JDK 21 or newer
- Maven 3.9+
- Eureka Server running at [http://localhost:8761](http://localhost:8761)

Start Eureka first:

```bash
cd Personal02/eureka-server
mvn spring-boot:run
```

## How to start

```bash
cd Personal02/api-gateway
mvn spring-boot:run
```

Or:

```bash
cd Personal02/api-gateway
mvn clean package
java -jar target/medicurex-api-gateway-0.0.1-SNAPSHOT.jar
```

The gateway listens on `http://localhost:8080`.

Run tests (Eureka is disabled in the test profile):

```bash
mvn test
```

## Port

| Application | Port | URL |
|---|---|---|
| `medicurex-api-gateway` | `8080` | `http://localhost:8080` |
| Health | `8080` | `http://localhost:8080/actuator/health` |
| `medicurex-eureka-server` | `8761` | `http://localhost:8761` |

The React frontend (`vite.config.js`) already proxies `/api` to `http://localhost:8080`. Keep the gateway on this port so the SPA does not need a new base URL.

`medicurex-auth` currently defaults to port `8080` as well. When the gateway and auth run together, start auth on a different port **without changing its source**, for example:

```bash
cd Personal02/Backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Auth must also register with Eureka as `medicurex-auth` before `/api/auth/**` can be load-balanced. Until then, those requests return `503` from the gateway. That is expected.

## Eureka dependency

The gateway is an Eureka **client**:

- `spring.application.name=medicurex-api-gateway`
- `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`
- `register-with-eureka=true`
- `fetch-registry=true`

Downstream URIs use `lb://<spring.application.name>`, not `localhost` or IP addresses. After a service registers, it should appear on the Eureka dashboard and the gateway can route to it.

Required Eureka application names:

| Gateway path | Eureka service name (`spring.application.name`) |
|---|---|
| `/api/auth/**` | `medicurex-auth` |
| `/api/profiles/**` | `medicurex-profile-service` |
| `/api/appointments/**` | `medicurex-appointment-service` |
| `/api/chat/**` | `medicurex-chat-service` |
| `/api/prescriptions/**` | `medicurex-prescription-service` |
| `/api/medicines/**` | `medicurex-pharmacy-service` |
| `/api/orders/**` | `medicurex-order-service` |

Paths are forwarded unchanged. Downstream controllers should map the same prefixes (for example Identity/Auth already uses `/api/auth`).

## Routes

| Predicate | Target |
|---|---|
| `Path=/api/auth/**` | `lb://medicurex-auth` |
| `Path=/api/profiles/**` | `lb://medicurex-profile-service` |
| `Path=/api/appointments/**` | `lb://medicurex-appointment-service` |
| `Path=/api/chat/**` | `lb://medicurex-chat-service` |
| `Path=/api/prescriptions/**` | `lb://medicurex-prescription-service` |
| `Path=/api/medicines/**` | `lb://medicurex-pharmacy-service` |
| `Path=/api/orders/**` | `lb://medicurex-order-service` |

The `Authorization` header is forwarded to downstream services so they can validate the JWT. The gateway does not parse or issue tokens.

## CORS

Allowed browser origins:

- `http://localhost:5173`
- `http://localhost:5174`

Allowed methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.  
`Authorization` is allowed and exposed. Credentials are enabled.

## Example requests

Health:

```bash
curl http://localhost:8080/actuator/health
```

Register (public, Identity/Auth):

```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Rahul\",\"email\":\"rahul@gmail.com\",\"password\":\"rahul123\",\"role\":\"patient\"}"
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"rahul@gmail.com\",\"password\":\"rahul123\"}"
```

Current user (JWT forwarded to Identity/Auth):

```bash
curl http://localhost:8080/api/auth/me ^
  -H "Authorization: Bearer <token>"
```

Other routed prefixes (503 until those services exist and register):

```bash
curl http://localhost:8080/api/profiles/me -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/appointments -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/chat -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/prescriptions -H "Authorization: Bearer <token>"
curl http://localhost:8080/api/medicines
curl http://localhost:8080/api/orders -H "Authorization: Bearer <token>"
```

## Dependencies

- Java 21
- Spring Boot 3.5.6
- Spring Cloud 2025.0.3 (Northfields)
- `spring-cloud-starter-gateway-server-webflux`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-boot-starter-actuator`

This gateway does not depend on MySQL, JPA, or the Identity/Auth codebase.
