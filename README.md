# Portfolio Agent Microservice

A Spring Boot 4.0.1 microservice application for managing portfolio agents with Java 17.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 4.0.1
- **Build Tool**: Maven
- **Database**: H2 (In-Memory)
- **Monitoring**: Prometheus
- **Documentation**: Swagger UI / OpenAPI 3.0

## Project Structure

```
portfolio-agent-microservice/
├── src/
│   ├── main/
│   │   ├── java/rgonzalez/agent/
│   │   │   ├── Microservice.java                 # Main application entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # Spring Security configuration
│   │   │   │   └── OpenApiConfig.java           # Swagger/OpenAPI configuration
│   │   │   ├── controller/
│   │   │   │   └── AgentController.java         # REST endpoints
│   │   │   ├── service/
│   │   │   │   └── AgentService.java            # Business logic
│   │   │   ├── repository/
│   │   │   │   └── AgentRepository.java         # Data access layer
│   │   │   └── entity/
│   │   │       ├── Agent.java                   # Agent entity
│   │   │       └── AgentStatus.java             # Agent status enum
│   │   └── resources/
│   │       └── application.properties           # Application configuration
│   └── test/
│       └── java/rgonzalez/agent/
│           └── controller/
│               └── AgentControllerTest.java     # Integration tests
├── pom.xml                                       # Maven configuration
└── README.md
```

## Build & Run

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Endpoints

### Agent Management

- **Create Agent**
  - `POST /api/agents`
  - Body: `{ "name": "string", "description": "string", "status": "ACTIVE|INACTIVE|SUSPENDED|MAINTENANCE" }`

- **Get All Agents**
  - `GET /api/agents`

- **Get Agent by ID**
  - `GET /api/agents/{id}`

- **Get Agents by Status**
  - `GET /api/agents/status/{status}`

- **Update Agent**
  - `PUT /api/agents/{id}`
  - Body: `{ "name": "string", "description": "string", "status": "string" }`

- **Delete Agent**
  - `DELETE /api/agents/{id}`

## Documentation & Monitoring

- **API Documentation**: `http://localhost:8080/api/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8080/api/api-docs`
- **Health Check**: `http://localhost:8080/api/actuator/health`
- **Metrics**: `http://localhost:8080/api/actuator/metrics`
- **Prometheus Metrics**: `http://localhost:8080/api/actuator/prometheus`
- **H2 Database Console**: `http://localhost:8080/api/h2-console`

## Default Credentials

- **H2 Database**: 
  - User: `sa`
  - Password: (empty)

- **API Authentication**:
  - Uses HTTP Basic Auth by default
  - Configure in `SecurityConfig.java`

## Dependencies

### Spring Boot Starters
- `spring-boot-starter-web` - Web and MVC support
- `spring-boot-starter-security` - Security features
- `spring-boot-starter-data-jpa` - JPA and database access
- `spring-boot-starter-validation` - Input validation
- `spring-boot-starter-actuator` - Application metrics and monitoring
- `spring-boot-devtools` - Development utilities
- `spring-boot-starter-test` - Testing framework

### Database & Storage
- `h2` - Embedded H2 database

### Monitoring
- `micrometer-registry-prometheus` - Prometheus metrics exporter

### JSON Processing
- `jackson-datatype-jsr310` - JSR-310 date/time support

### Documentation
- `springdoc-openapi-starter-webmvc-ui` - Swagger UI and OpenAPI 3.0

## Configuration

Edit `src/main/resources/application.properties` to customize:
- Server port
- Database settings
- JPA/Hibernate properties
- Actuator endpoints
- Logging levels

## Testing

Run tests using Maven:

```bash
mvn test
```

Integration tests are provided for the Agent Controller with authenticated requests.

## Development

The project includes Spring Boot DevTools for rapid development with:
- Automatic restart on code changes
- Live reload
- Configurations for enhanced development experience

## License

Apache License 2.0