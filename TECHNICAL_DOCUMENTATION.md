# Jackpot Service - Comprehensive Technical Documentation

**Version:** 1.0  
**Date:** December 29, 2025  
**Author:** Development Team  
**Language:** English (Professional)

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Software Engineering Principles](#software-engineering-principles)
3. [Design Patterns Used](#design-patterns-used)
4. [File-by-File Technical Breakdown](#file-by-file-technical-breakdown)
5. [Data Flow and Interactions](#data-flow-and-interactions)
6. [Computer Science Concepts](#computer-science-concepts)
7. [Best Practices and Rationale](#best-practices-and-rationale)

---

## Architecture Overview

### System Architecture: Layered (N-Tier) Architecture

The Jackpot Service follows a **layered architecture**, which is one of the most common architectural patterns in enterprise applications. This architecture separates concerns into distinct layers, each with specific responsibilities.

```
┌─────────────────────────────────────────┐
│        Presentation Layer               │
│  (REST Controllers via Spring Web)      │
├─────────────────────────────────────────┤
│        Application Layer                │
│  (DTOs - Data Transfer Objects)         │
├─────────────────────────────────────────┤
│        Business Logic Layer             │
│  (Service Classes - Core Logic)         │
├─────────────────────────────────────────┤
│        Data Access Layer                │
│  (Repositories - JPA/Hibernate)         │
├─────────────────────────────────────────┤
│        Persistence Layer                │
│  (Entities - Domain Models)             │
├─────────────────────────────────────────┤
│        Database Layer                   │
│  (PostgreSQL Relational Database)       │
└─────────────────────────────────────────┘
```

### Why Layered Architecture?

- **Separation of Concerns**: Each layer has a single responsibility
- **Testability**: Layers can be tested independently using mocks
- **Maintainability**: Changes in one layer don't ripple through the entire system
- **Scalability**: Layers can be scaled independently
- **Clarity**: The codebase is easier to understand and navigate

---

## Software Engineering Principles

### 1. SOLID Principles

#### **S - Single Responsibility Principle (SRP)**
Each class has exactly one reason to change.

- **JackpotController**: Handles HTTP requests/responses for jackpot operations
- **JackpotService**: Handles business logic for jackpots
- **JackpotRepository**: Handles database queries for jackpots
- **Jackpot Entity**: Represents a jackpot domain object

**Benefit**: If business logic changes, only the Service layer needs modification. The Controller doesn't need to change.

#### **O - Open/Closed Principle (OCP)**
Classes should be open for extension but closed for modification.

- We use inheritance and abstraction (Spring repositories extend JpaRepository)
- New exception types can be added by extending Exception without modifying GlobalExceptionHandler

#### **L - Liskov Substitution Principle (LSP)**
Subtypes must be substitutable for their base types.

- All repositories implement JpaRepository interface and can be used interchangeably

#### **I - Interface Segregation Principle (ISP)**
Clients should depend on fine-grained interfaces, not bloated ones.

- Services inject only the repositories they need (not a "god repository")

#### **D - Dependency Inversion Principle (DIP)**
High-level modules should depend on abstractions, not low-level modules.

- **BetService** depends on repository interfaces, not concrete implementations
- This is achieved through Spring's **Dependency Injection**

### 2. DRY (Don't Repeat Yourself)
Code reusability is maximized:
- DTO mapping logic in services (mapToDto methods)
- Consistent error handling through GlobalExceptionHandler
- Shared validation logic

### 3. KISS (Keep It Simple, Stupid)
Code is straightforward and easy to understand:
- No unnecessary complexity
- Clear variable and method names
- Single-purpose methods

### 4. YAGNI (You Aren't Gonna Need It)
No speculative features added:
- Only the required endpoints are implemented
- No unused utility classes
- No unnecessary abstractions

---

## Design Patterns Used

### 1. **Layered Architecture Pattern**
Organizes code into layers, each with specific responsibilities. Already discussed above.

### 2. **Repository Pattern**
Abstracts data access logic.

**Why?**
- Decouples business logic from database queries
- Makes testing easier (can mock repositories)
- Changes to database query logic don't affect business logic

**Example:**
```java
// Business logic doesn't know about SQL
Jackpot jackpot = jackpotRepository.findById(jackpotId);

// Repository handles the SQL query
// If we switch from PostgreSQL to MongoDB, only Repository changes
```

### 3. **Data Transfer Object (DTO) Pattern**
Uses DTOs to transfer data between layers.

**Why?**
- Entities shouldn't be exposed directly to API clients
- API contract is decoupled from database schema
- Can control which fields are visible to clients

**Example:**
- **Jackpot Entity** (internal) has all fields
- **JackpotDto** (external) exposes only: id, name, currentSize, winCount, lastWinTimestamp
- If we add an internal field to Jackpot, the API response doesn't change

### 4. **Service Layer Pattern**
Encapsulates business logic in service classes.

**Why?**
- Controllers delegate to services (thin controllers)
- Business logic is reusable across multiple controllers
- Easier to test business logic

### 5. **Dependency Injection (DI) Pattern**
Spring automatically injects dependencies.

**Why?**
- Reduces coupling between classes
- Easier to test (can inject mock objects)
- Configuration is centralized

**Example:**
```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class BetService {
    private final BetRepository betRepository;  // Injected by Spring
    
    // No need to do: betRepository = new BetRepositoryImpl()
}
```

### 6. **Exception Handling Pattern (Centralized)**
Uses @RestControllerAdvice for global exception handling.

**Why?**
- Consistent error responses across all endpoints
- Exception handling logic is centralized
- Reduces code duplication

### 7. **Builder Pattern**
Uses Lombok @Builder for object construction.

```java
Jackpot jackpot = Jackpot.builder()
    .name("Mega Jackpot")
    .winProbability(0.15)
    .build();
```

**Why?**
- Readable and fluent API
- Handles optional fields elegantly
- Reduces boilerplate constructors

---

## File-by-File Technical Breakdown

### **1. JackpotApplication.java**
**Location:** `src/main/java/com/example/jackpot/`

**What it does:**
- Entry point of the Spring Boot application
- Starts the embedded Tomcat server on port 8080
- Initializes Spring context and loads all beans

**Why it's needed:**
- Every Spring Boot application needs a main class with @SpringBootApplication annotation
- This annotation combines three important annotations:
  - @Configuration: Marks class as a configuration source
  - @EnableAutoConfiguration: Tells Spring to guess bean definitions based on classpath
  - @ComponentScan: Scans for @Component, @Service, @Repository, @Controller classes

**Code Structure:**
```java
@SpringBootApplication
public class JackpotApplication {
    public static void main(String[] args) {
        SpringApplication.run(JackpotApplication.class, args);
    }
}
```

**Computer Science Concept:**
- **Bootstrapping**: Initializing the runtime environment and loading necessary components
- **Inversion of Control (IoC)**: Spring controls the lifecycle of objects, not the application

---

### **2. Entity Classes (Domain Models)**

#### **2.1 Jackpot.java**
**Location:** `src/main/java/com/example/jackpot/entity/`

**What it does:**
- Represents a jackpot in the domain
- Maps to "jackpots" table in PostgreSQL
- Stores state: name, win probability, current size, win count, last win timestamp

**Why it's needed:**
- **ORM Mapping**: Hibernate/JPA automatically maps this class to a database table
- **Type Safety**: Java object instead of raw SQL provides compile-time type checking
- **Business Logic Container**: Can add validation methods

**Key Fields:**
```java
@Id @GeneratedValue(strategy = GenerationType.UUID)
private UUID id;  // Primary key (unique identifier)

@Column(nullable = false)
private String name;  // Cannot be null in database

@Column(nullable = false)
private Double winProbability;  // 0.0 to 1.0

@Column(nullable = false)
@Builder.Default
private BigDecimal currentSize = BigDecimal.ZERO;  // Money, use BigDecimal not Double
```

**Design Decisions:**

1. **UUID instead of Auto-Increment Integer**
   - Pros: Globally unique, cannot be guessed, distributed-system friendly
   - Cons: Larger storage size (128 bits vs 32 bits)
   - Decision: UUID chosen for security and distributed scenarios

2. **BigDecimal for Money**
   - Never use Double for money (floating-point precision errors)
   - BigDecimal provides exact decimal arithmetic
   - Critical for financial calculations

3. **LocalDateTime for Timestamps**
   - Better than java.util.Date (which is legacy)
   - Timezone-aware operations
   - Part of Java 8+ java.time API

**Computer Science Concepts:**
- **Object-Relational Mapping (ORM)**: Bridges gap between object-oriented code and relational databases
- **Entity-Relationship Model**: Each entity represents a business object
- **Primary Key**: Unique identifier for each row

---

#### **2.2 Bet.java**
**Location:** `src/main/java/com/example/jackpot/entity/`

**What it does:**
- Represents a player's bet on a jackpot
- Maps to "bets" table
- Creates an audit trail of all bets

**Why it's needed:**
- **Immutable Record**: Every bet must be recorded (audit trail)
- **Relationship**: Links player to jackpot
- **Historical Data**: Enables analytics ("most active players", etc.)

**Key Field:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "jackpot_id", nullable = false)
private Jackpot jackpot;  // Foreign key relationship
```

**Design Decision: FetchType.LAZY**
- Only loads jackpot from database when accessed
- Improves performance (avoids N+1 query problem)
- Alternative EAGER would load jackpot immediately (wasteful)

**Computer Science Concepts:**
- **Foreign Key**: Links related tables
- **Lazy Loading**: Load data only when needed
- **Immutability**: createdAt cannot be updated (updatable = false)

---

#### **2.3 Win.java**
**Location:** `src/main/java/com/example/jackpot/entity/`

**What it does:**
- Represents a successful jackpot win
- Maps to "wins" table
- Records winner, amount, and timestamp

**Why it's needed:**
- **Historical Record**: Track all wins
- **Analytics**: Analyze win patterns
- **Audit Trail**: Verify jackpot resets

**Key Field:**
```java
@Builder.Default
private LocalDateTime timestamp = LocalDateTime.now();
```

The `@Builder.Default` ensures timestamp is set to current time unless explicitly provided.

---

### **3. Repository Classes (Data Access Layer)**

#### **3.1 JackpotRepository.java**
**Location:** `src/main/java/com/example/jackpot/repository/`

**What it does:**
- Provides database access for Jackpot entities
- Extends Spring Data JpaRepository (no implementation needed)

**Why it's needed:**
- Abstracts SQL queries
- Spring generates SQL automatically
- Allows switching databases without changing business logic

**Code:**
```java
@Repository
public interface JackpotRepository extends JpaRepository<Jackpot, UUID> {
}
```

**How it works:**
Spring automatically implements CRUD methods:
- `save(jackpot)` - INSERT or UPDATE
- `findById(id)` - SELECT WHERE id = ?
- `findAll()` - SELECT * FROM jackpots
- `delete(jackpot)` - DELETE
- `count()` - SELECT COUNT(*)

**Computer Science Concepts:**
- **Repository Pattern**: Mediates between domain and mapping layers
- **Abstraction**: Hides database details from business logic
- **CRUD Operations**: Create, Read, Update, Delete

#### **3.2 BetRepository.java**
**Location:** `src/main/java/com/example/jackpot/repository/`

**What it does:**
- Provides database access for Bet entities

**Why it's needed:**
- Records every bet to database
- Enables historical analysis

---

#### **3.3 WinRepository.java**
**Location:** `src/main/java/com/example/jackpot/repository/`

**What it does:**
- Provides database access for Win entities
- Supports pagination queries

**Code:**
```java
@Repository
public interface WinRepository extends JpaRepository<Win, UUID> {
}
```

**Note:** JpaRepository already includes pagination support through Page<T> return type.

---

### **4. Service Classes (Business Logic Layer)**

#### **4.1 JackpotService.java**
**Location:** `src/main/java/com/example/jackpot/service/`

**What it does:**
- Core business logic for jackpot operations
- Orchestrates repositories
- Maps entities to DTOs

**Why it's needed:**
- Controllers should NOT contain business logic
- Services are reusable (can be called from other services)
- Easier to test (mock dependencies)

**Key Methods:**

```java
public JackpotDto createJackpot(CreateJackpotRequest request)
```
- Creates a new jackpot from request
- Maps request to entity
- Saves to database using repository
- Maps entity to DTO for response

```java
public List<JackpotDto> getAllJackpots()
```
- Retrieves all jackpots from database
- Maps entities to DTOs
- Returns list

```java
protected void resetJackpotSize(Jackpot jackpot)
```
- Helper method to reset jackpot to zero (called after win)
- Protected scope (only this service and subclasses can call)

**Annotation: @Transactional**
```java
@Service
@RequiredArgsConstructor
@Transactional  // All methods are transactional by default
public class JackpotService {
```

**Why @Transactional?**
- Ensures ACID properties for database operations
- If exception occurs, database changes are rolled back
- Critical for data consistency

**Computer Science Concepts:**
- **Service Layer**: Business logic tier
- **Transactions**: Ensures data consistency
- **DTOs**: Decouple internal model from API contract

---

#### **4.2 BetService.java**
**Location:** `src/main/java/com/example/jackpot/service/`

**What it does:**
- Most critical business logic
- Places bets, determines wins, updates jackpots
- Manages complete bet workflow

**Why it's needed:**
- Complex business logic (cannot be in controller)
- Orchestrates multiple repositories

**Key Method: placeBet()**

**Workflow:**
1. Validate bet amount (must be positive)
2. Load jackpot from database
3. Create and save bet record (always persisted)
4. Add bet amount to jackpot size
5. Determine win (random)
6. If won:
   - Create win record
   - Reset jackpot to zero
   - Increment win count
   - Update last win timestamp
7. If lost:
   - Keep accumulated bet amount in jackpot
8. Save updated jackpot
9. Return response to controller

**Critical Method: determineWin()**
```java
private boolean determineWin(Double winProbability) {
    return random.nextDouble() < winProbability;
}
```

**Why this logic?**
- `random.nextDouble()` generates random number [0.0, 1.0)
- If probability is 0.15, only 15% of calls return true
- Simple, efficient, mathematically correct

**Computer Science Concepts:**
- **Random Number Generation**: Pseudo-random using seed
- **Probability**: Mathematical foundation for gambling logic
- **Transaction Isolation**: Multiple transactions can run in parallel without conflicts

---

#### **4.3 WinService.java**
**Location:** `src/main/java/com/example/jackpot/service/`

**What it does:**
- Retrieves win records with pagination
- Maps entities to DTOs

**Why it's needed:**
- Separates win retrieval logic from controller
- Handles pagination complexity

**Key Method: getWins()**
```java
public List<WinDto> getWins(int limit, int offset) {
    Pageable pageable = PageRequest.of(offset, limit, Sort.by("timestamp").descending());
    Page<Win> wins = winRepository.findAll(pageable);
    
    return wins.getContent()
        .stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());
}
```

**Pagination Explanation:**
- **limit**: How many results per page (e.g., 10)
- **offset**: Which page (0-based, so offset=1 means page 2)
- **Sort**: Order results (descending = newest first)

**Calculation:**
```
offset=0, limit=10 → records 0-9
offset=1, limit=10 → records 10-19
offset=2, limit=10 → records 20-29
```

**Computer Science Concepts:**
- **Pagination**: Efficient data retrieval for large datasets
- **Sorting**: Database-level ordering (efficient)
- **Streams API**: Functional programming in Java (maps, filters, collects)

---

### **5. Controller Classes (Presentation Layer)**

#### **5.1 JackpotController.java**
**Location:** `src/main/java/com/example/jackpot/controller/`

**What it does:**
- Handles HTTP requests/responses for jackpot operations
- Maps HTTP methods to service calls
- Returns appropriate HTTP status codes

**Why it's needed:**
- REST API entry point
- Controllers should be "thin" (minimal logic)
- Delegate to services

**Key Methods:**

```java
@PostMapping
public ResponseEntity<JackpotDto> createJackpot(@RequestBody CreateJackpotRequest request) {
    JackpotDto jackpot = jackpotService.createJackpot(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(jackpot);
}
```

**HTTP Details:**
- Method: POST (creates new resource)
- Status Code: 201 CREATED (not 200 OK)
- Body: CreateJackpotRequest JSON
- Response: JackpotDto JSON with HTTP 201

**REST Design:**
```
HTTP Method  | URL                    | Status  | Purpose
─────────────┼────────────────────────┼─────────┼──────────────
POST         | /api/jackpots          | 201     | Create new
GET          | /api/jackpots          | 200     | List all
GET          | /api/jackpots/{id}     | 200     | Get specific
PUT          | /api/jackpots/{id}     | 200     | Update
DELETE       | /api/jackpots/{id}     | 204     | Delete
```

**Computer Science Concepts:**
- **REST Principles**: Resource-oriented architecture
- **HTTP Status Codes**: Semantic meaning
- **Request/Response Pattern**: Standard web communication

#### **5.2 BetController.java**
**Location:** `src/main/java/com/example/jackpot/controller/`

**What it does:**
- Handles bet placement HTTP requests
- Delegates to BetService
- Returns bet result

**Why it's needed:**
- API endpoint for placing bets

---

#### **5.3 WinController.java**
**Location:** `src/main/java/com/example/jackpot/controller/`

**What it does:**
- Handles win retrieval HTTP requests
- Supports pagination query parameters
- Returns list of wins

**Why it's needed:**
- API endpoint for retrieving win history

---

### **6. DTO Classes (Data Transfer Objects)**

#### **Why DTOs?**

**Problem without DTOs:**
```java
// Exposing entity directly
@GetMapping
public Jackpot getJackpot(UUID id) {
    return jackpotRepository.findById(id).get();
}
```
Issues:
- Exposes internal fields (e.g., createdAt)
- Changes to entity affect API contract
- Security risk (exposes sensitive data)

**Solution with DTOs:**
```java
@GetMapping
public JackpotDto getJackpot(UUID id) {
    Jackpot jackpot = jackpotService.getJackpotById(id);
    return mapToDto(jackpot);  // Only expose selected fields
}
```

#### **6.1 CreateJackpotRequest.java**
- Represents incoming POST /api/jackpots request body
- Only contains fields needed for creation
- Validated by Spring's validation framework

#### **6.2 JackpotDto.java**
- Represents outgoing jackpot response
- Contains only fields suitable for API clients
- Excludes internal fields like createdAt

#### **6.3 BetRequest.java**
- Represents incoming POST /api/bets request
- Contains: jackpotId, playerAlias, betAmount

#### **6.4 BetResponse.java**
- Represents bet result
- Contains: won, winAmount, newJackpotSize, message
- Provides user-friendly feedback

#### **6.5 WinDto.java**
- Represents a win record in API response
- Contains: timestamp, playerAlias, winAmount

#### **6.6 ErrorResponse.java**
- Represents error response
- Contains: status (HTTP code), message, error type, timestamp
- Consistent error format across all endpoints

**Computer Science Concepts:**
- **Encapsulation**: Hide internal details
- **API Contract**: Decoupled from implementation
- **Single Responsibility**: DTOs only transfer data

---

### **7. Exception Handling**

#### **7.1 GlobalExceptionHandler.java**
**Location:** `src/main/java/com/example/jackpot/exception/`

**What it does:**
- Centralized exception handling for all endpoints
- Catches exceptions and returns consistent error responses
- Logs errors for debugging

**Why it's needed:**
- DRY principle: handle errors once, not in every controller
- Consistent error format
- Separates error handling from business logic

**Key Annotation: @RestControllerAdvice**
- Applies to all REST controllers
- Intercepts exceptions before returning to client

**Exception Handlers:**

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    // Return 400 Bad Request for business logic errors
}

@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    // Return 500 Internal Server Error for unexpected errors
}
```

**HTTP Status Codes Used:**
- **400 Bad Request**: Illegal argument (validation failure)
- **500 Internal Server Error**: Unexpected system errors

**Computer Science Concepts:**
- **Exception Handling Strategy**: Centralized vs distributed
- **Error Responses**: Meaningful feedback to clients
- **Logging**: Recording errors for analysis

---

### **8. Configuration**

#### **8.1 OpenApiConfig.java**
**Location:** `src/main/java/com/example/jackpot/config/`

**What it does:**
- Configures OpenAPI/Swagger documentation
- Provides API metadata (title, description, version, contact)

**Why it's needed:**
- Auto-generates Swagger UI at `/swagger-ui/index.html`
- Provides interactive API documentation
- Accessible at `/v3/api-docs` as JSON schema

**Code:**
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI jackpotOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Jackpot Service API")
                .description("API for managing jackpots, placing bets, and tracking wins")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Jackpot Service")
                    .email("support@jackpot.example.com")));
    }
}
```

**Computer Science Concepts:**
- **API Documentation**: Essential for client developers
- **OpenAPI Standard**: Industry-standard API specification
- **Metadata**: Describing APIs programmatically

---

### **9. Application Configuration**

#### **9.1 application.properties**
**Location:** `src/main/resources/`

**What it does:**
- Configures Spring Boot application settings
- Database connection details
- JPA/Hibernate settings

**Key Properties:**
```properties
spring.application.name=jackpot
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://postgres:5432/jackpot
spring.datasource.username=jackpot
spring.datasource.password=jackpot
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Actuator (Health/Metrics)
management.endpoints.web.exposure.include=health,metrics
```

**Key Settings Explained:**

- **spring.datasource.url**: JDBC URL (host=postgres because Docker Compose service)
- **spring.jpa.hibernate.ddl-auto=update**: Automatically create/update database tables from entities
  - validate: check only
  - update: create/modify tables
  - create: always create (drops existing)
  - create-drop: create and drop on shutdown

**Computer Science Concepts:**
- **Configuration Management**: Externalizing configuration
- **Environment Variables**: Docker-friendly configuration
- **Connection Pooling**: Managing database connections efficiently

---

### **10. Build Configuration**

#### **10.1 pom.xml**
**Location:** Root directory

**What it does:**
- Maven configuration file
- Declares dependencies
- Configures build process

**Key Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
Provides Spring Web MVC for REST controllers

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
Provides Hibernate ORM and Spring Data JPA

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```
PostgreSQL JDBC driver (runtime only, not compiled into JAR)

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```
Reduces boilerplate code (@Data, @Builder, @RequiredArgsConstructor)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```
Generates Swagger UI and OpenAPI documentation

**Computer Science Concepts:**
- **Dependency Management**: Maven resolves transitive dependencies
- **Build Automation**: Compiles, tests, packages automatically
- **Version Management**: Ensures compatibility

---

### **11. Containerization**

#### **11.1 Dockerfile**
**Location:** Root directory

**What it does:**
- Builds Docker image for the application
- Multi-stage build for optimization

**Stages:**

**Stage 1: Builder**
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve
COPY src ./src
RUN mvn clean package -DskipTests
```
- Compiles application using Maven
- Creates JAR file
- Large image (contains Maven, source code)

**Stage 2: Runtime**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
- Starts from small JRE image
- Copies only the compiled JAR
- Discards Maven, source code
- Small final image (~200 MB vs ~1 GB)

**Why Multi-Stage?**
- Keeps production image small
- Faster deployment
- Less storage usage

**Computer Science Concepts:**
- **Containerization**: Application packaging
- **Image Layers**: Caching for build optimization
- **Base Images**: Minimal runtime dependencies

---

#### **11.2 docker-compose.yml**
**Location:** Root directory

**What it does:**
- Orchestrates multi-container deployment
- Starts PostgreSQL and Spring Boot application
- Creates networking between containers

**Services:**

```yaml
jackpot-postgres:
  image: postgres:16
  environment:
    POSTGRES_DB: jackpot
    POSTGRES_USER: jackpot
    POSTGRES_PASSWORD: jackpot
  volumes:
    - postgres_data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U jackpot"]
    interval: 5s
    timeout: 5s
    retries: 5
```

- **image**: PostgreSQL version 16
- **environment**: Database credentials
- **volumes**: Persists data across restarts
- **healthcheck**: Ensures database is ready before app starts

```yaml
jackpot-app:
  build: .
  ports:
    - "8080:8080"
  depends_on:
    jackpot-postgres:
      condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://jackpot-postgres:5432/jackpot
```

- **build**: Builds image from Dockerfile
- **ports**: Maps port 8080 container to host
- **depends_on**: Waits for database health check
- **environment**: Overrides application.properties

**Computer Science Concepts:**
- **Orchestration**: Managing multi-container deployment
- **Service Discovery**: Containers communicate by service name
- **Health Checks**: Ensuring service readiness

---

### **12. Documentation**

#### **12.1 README.md**
**Location:** Root directory

**What it does:**
- Provides quick start instructions
- Documents API endpoints with examples
- Lists database schema
- Explains project structure

**Sections:**
- Prerequisites and installation
- How to run locally and with Docker
- API endpoint documentation with curl/HTTP examples
- Database schema explanation
- Project structure overview
- Implementation details
- Production considerations

---

## Data Flow and Interactions

### **Complete Bet Placement Flow**

```
1. HTTP Request (Browser/Postman)
   POST /api/bets
   {
     "jackpotId": "729ac12b-a40d-41cc-ac13-f34faa2fb7c7",
     "playerAlias": "player999",
     "betAmount": 100
   }
          ↓
2. Spring DispatcherServlet routes to Controller
          ↓
3. BetController.placeBet()
   - Validates request body (Spring does this automatically)
   - Calls betService.placeBet(request)
          ↓
4. BetService.placeBet()
   - Validates bet amount > 0
   - Loads Jackpot from JackpotRepository
   - Creates Bet entity, saves via BetRepository
   - Adds bet amount to jackpot size
   - Calls determineWin() → random logic
   - If won:
     * Creates Win entity, saves via WinRepository
     * Resets jackpot to 0
     * Increments winCount
     * Updates lastWinTimestamp
   - Saves updated Jackpot via JackpotRepository
   - Returns BetResponse
          ↓
5. BetController returns ResponseEntity<BetResponse>
          ↓
6. Jackson (Spring's JSON library) serializes response
          ↓
7. HTTP Response (Status 200)
   {
     "won": true,
     "winAmount": 100,
     "newJackpotSize": 0,
     "message": "Congratulations! You won!"
   }
          ↓
8. Browser/Postman displays response
```

### **Database Interaction**

```
BetService.placeBet()
    │
    ├─→ jackpotRepository.findById(id)
    │   └─→ SELECT * FROM jackpots WHERE id = ?
    │
    ├─→ betRepository.save(bet)
    │   └─→ INSERT INTO bets (id, jackpot_id, player_alias, bet_amount, created_at) VALUES (...)
    │
    ├─→ winRepository.save(win)  [if won]
    │   └─→ INSERT INTO wins (id, jackpot_id, player_alias, win_amount, timestamp) VALUES (...)
    │
    └─→ jackpotRepository.save(jackpot)
        └─→ UPDATE jackpots SET current_size = ?, win_count = ?, last_win_timestamp = ? WHERE id = ?
```

---

## Computer Science Concepts

### **1. Object-Oriented Programming (OOP)**

#### **Encapsulation**
- Data (fields) are private
- Access through public methods (getters/setters via Lombok)
- Example: Jackpot.currentSize is private, accessed via getters

#### **Inheritance**
- Repositories inherit from JpaRepository
- Allows polymorphic behavior

#### **Polymorphism**
- Different exception types handled by different handlers
- Repositories provide same interface but different implementations

#### **Abstraction**
- Service classes abstract business logic
- Controllers don't know how services implement logic

### **2. Design Principles**

#### **DRY (Don't Repeat Yourself)**
- DTO mapping methods reused
- Exception handling centralized
- Validation logic centralized

#### **SOLID Principles**
Already covered above

### **3. Database Theory**

#### **Normalization**
- Tables properly normalized (each fact stored once)
- Jackpots, Bets, Wins are separate tables
- Relationships through foreign keys

#### **ACID Properties**
- **Atomicity**: Transaction fully completes or fully rolls back
- **Consistency**: Data valid before and after transaction
- **Isolation**: Concurrent transactions don't interfere
- **Durability**: Committed data persists

#### **Transactions**
- @Transactional ensures ACID compliance
- If exception occurs, changes rolled back

### **4. Concurrency**

#### **Thread Safety**
- Spring Data JPA handles concurrent transactions
- Each HTTP request is handled by separate thread
- No shared mutable state

#### **Isolation Levels**
- Default isolation level prevents dirty reads

### **5. REST Architecture**

#### **Resource-Oriented**
- Jackpots, Bets, Wins are resources
- Identified by URLs (/api/jackpots, /api/bets)

#### **Standard HTTP Methods**
- GET: Retrieve
- POST: Create
- PUT: Update
- DELETE: Delete

#### **Stateless**
- Server doesn't store client context
- Each request is independent

### **6. Random Number Generation**

#### **Pseudo-Random**
- Java's Random uses deterministic algorithm with seed
- Good for simulation, not cryptography

#### **Probability**
- random.nextDouble() returns [0.0, 1.0)
- If probability is 0.15, approximately 15% of bets win
- Statistically correct over large sample

### **7. Time Handling**

#### **LocalDateTime**
- No timezone information (local to server)
- Sufficient for this use case
- Better than java.util.Date (legacy)

#### **Timestamp**
- Recorded at bean creation (@Builder.Default)
- Immutable (cannot be changed after creation)

---

## Best Practices and Rationale

### **1. Code Organization**
```
src/main/java/com/example/jackpot/
├── JackpotApplication.java          (entry point)
├── controller/                       (HTTP handlers)
├── service/                          (business logic)
├── repository/                       (data access)
├── entity/                           (domain models)
├── dto/                              (data transfer)
├── exception/                        (error handling)
└── config/                           (configuration)
```

**Rationale:**
- Clear separation by responsibility
- Easy to locate code
- Scalable as project grows

### **2. Naming Conventions**
- Classes: PascalCase (JackpotService)
- Methods: camelCase (placeBet)
- Constants: UPPER_CASE (DEFAULT_LIMIT)
- Database: snake_case (current_size)

**Rationale:**
- Follows Java conventions
- Consistent with industry standards
- Improves readability

### **3. Immutable Objects**
- Timestamps are @Builder.Default, cannot be modified
- Data transfer objects are simple containers
- Entity IDs are never changed

**Rationale:**
- Prevents accidental modifications
- Thread-safe without locking
- Clear intent

### **4. Validation**
- Input validation in service layer
- Spring validation framework ready for expansion
- Clear error messages

**Rationale:**
- Business logic validates its inputs
- Controllers are thin (don't validate)
- Users receive helpful feedback

### **5. Logging**
- Errors logged with full stack trace
- Info level for important events
- Using SLF4J (industry standard)

**Rationale:**
- Debugging production issues
- Audit trail
- Performance monitoring

### **6. Documentation**
- JavaDoc comments on public methods
- Clear class-level documentation
- Example requests/responses in README

**Rationale:**
- Future developers understand intent
- API clients know how to use endpoints
- Reduces onboarding time

### **7. Testing Strategy**
(Not implemented, but recommended):
```java
@SpringBootTest
public class BetServiceTest {
    @MockBean
    private JackpotRepository jackpotRepository;
    
    @Test
    public void testPlaceBetWhenWon() {
        // Mock jackpot
        // Call placeBet
        // Verify win recorded
        // Verify jackpot reset
    }
}
```

**Rationale:**
- Service logic is testable
- Repositories can be mocked
- No database needed for unit tests

---

## Summary: Why This Architecture?

### **For the Business:**
✅ Meets all requirements (4 endpoints)  
✅ Single command deployment (docker compose up)  
✅ Scalable if requirements grow  
✅ Easy to maintain  

### **For the Developer:**
✅ Clear code organization  
✅ Easy to debug  
✅ Easy to test  
✅ Easy to extend  

### **For the Operations:**
✅ Docker containerized  
✅ Persistent database  
✅ Health checks  
✅ Proper error handling  

---

## Conclusion

The Jackpot Service demonstrates professional software engineering through:

1. **Layered Architecture** - Separation of concerns
2. **SOLID Principles** - Maintainable code
3. **Design Patterns** - Proven solutions
4. **Spring Framework** - Industry-standard stack
5. **Docker** - Container-native deployment
6. **Documentation** - Clear communication
7. **Best Practices** - Following conventions

This foundation is suitable for:
- Production deployment (with added security/monitoring)
- Team development (code is clear and maintainable)
- Future extensions (architecture supports growth)
- Job interviews (demonstrates CS fundamentals)

---

## Further Learning Resources

- **Spring Documentation**: https://spring.io/projects/spring-boot
- **SOLID Principles**: Uncle Bob's Clean Code
- **Design Patterns**: Gang of Four "Design Patterns"
- **RESTful API Design**: REST API Best Practices
- **Docker**: Docker Official Documentation
- **PostgreSQL**: PostgreSQL Official Docs

---

**End of Technical Documentation**

*This documentation serves as a comprehensive guide to understanding the Jackpot Service implementation, architectural decisions, and underlying software engineering principles.*
