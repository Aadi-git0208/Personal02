# MediCurex Eureka Server

Service discovery registry for the MediCurex SOA platform.

This application is a standalone Netflix Eureka server. It has no business APIs, no database, and no authentication logic. Other MediCurex services register here so the API gateway and peer services can locate them by name.

## Prerequisites

- JDK 21 or newer
- Maven 3.9+

No MySQL, Redis, or message broker is required.

## How to run

From this folder:

```bash
cd Personal02/eureka-server
mvn spring-boot:run
```

Or:

```bash
cd Personal02/eureka-server
mvn clean package
java -jar target/medicurex-eureka-server-0.0.1-SNAPSHOT.jar
```

The registry starts at `http://localhost:8761`.

Run tests:

```bash
mvn test
```

## Eureka dashboard URL

Open the Eureka dashboard in a browser:

[http://localhost:8761](http://localhost:8761)

Registered MediCurex services appear under **Instances currently registered with Eureka**. Until other services are started, this list is empty. That is expected.

## Ports

| Service | Port | URL |
|---|---|---|
| `medicurex-eureka-server` | `8761` | `http://localhost:8761` |
| Eureka registry API | `8761` | `http://localhost:8761/eureka/` |

## How other MediCurex services should register

Do **not** add Eureka client dependencies to this server. Client registration belongs in the other services (`medicurex-auth`, gateway, profile, appointment, and so on).

In each client service:

1. Use Spring Boot 3.5.x and Spring Cloud 2025.0.x (Northfields).
2. Add the Eureka **client** starter:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

3. Import the Spring Cloud BOM (same train as this server):

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2025.0.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

4. Configure the client `application.properties`:

```properties
spring.application.name=medicurex-auth
server.port=8080

eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
```

`spring.application.name` is the name other services and the gateway use to look up this instance. Use a unique port per service.

5. Start **this Eureka server first**, then start the client services.

After a client is healthy, it should appear on the dashboard within about 30 seconds (Eureka's default heartbeat interval).

## Configuration

| Property | Value | Purpose |
|---|---|---|
| `spring.application.name` | `medicurex-eureka-server` | Application name |
| `server.port` | `8761` | Dashboard and registry port |
| `eureka.client.register-with-eureka` | `false` | Server does not register with itself |
| `eureka.client.fetch-registry` | `false` | Server does not fetch a remote registry |
| `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka/` | Local registry URL for reference |

## Dependencies

- Java 21
- Spring Boot 3.5.6
- Spring Cloud 2025.0.3 (Northfields)
- `spring-cloud-starter-netflix-eureka-server`

This server does not depend on the Identity/Auth Service, MySQL, or the frontend.
