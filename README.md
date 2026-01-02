# Jackpot Service

A Spring Boot microservice for managing jackpots and player bets.

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 17 (for local development)
- Maven 3.9+

### Run with Docker Compose

```bash
docker compose up
```

The application will be available at:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

### Build and Run Locally

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

PostgreSQL connection details (update in `src/main/resources/application.properties`):
- URL: `jdbc:postgresql://localhost:5432/jackpot`
- Username: `jackpot`
- Password: `jackpot`

### Testing

```bash
# Run all unit and integration tests (H2 in-memory)
mvn test
```

## API Endpoints

### 1. Create a Jackpot
```bash
POST /api/jackpots
Content-Type: application/json

{
  "name": "Mega Jackpot",
  "winProbability": 0.15
}
```

**Response (201 Created):**
```json
{
  "id": "729ac12b-a40d-41cc-ac13-f34faa2fb7c7",
  "name": "Mega Jackpot",
  "currentSize": 0,
  "winCount": 0,
  "lastWinTimestamp": null
}
```

### 2. Get All Jackpots
```bash
GET /api/jackpots
```

**Response (200 OK):**
```json
[
  {
    "id": "729ac12b-a40d-41cc-ac13-f34faa2fb7c7",
    "name": "Mega Jackpot",
    "currentSize": 1000.00,
    "winCount": 0,
    "lastWinTimestamp": null
  }
]
```

### 3. Place a Bet
```bash
POST /api/bets
Content-Type: application/json

{
  "jackpotId": "729ac12b-a40d-41cc-ac13-f34faa2fb7c7",
  "playerAlias": "player999",
  "betAmount": 100
}
```

**Response (200 OK) - Loss:**
```json
{
  "won": false,
  "winAmount": 0,
  "newJackpotSize": 1100.00,
  "message": "Better luck next time!"
}
```

**Response (200 OK) - Win:**
```json
{
  "won": true,
  "winAmount": 1100.00,
  "newJackpotSize": 0,
  "message": "Congratulations! You won!"
}
```

### 4. Get Wins
```bash
GET /api/wins?limit=10&offset=0
```

**Response (200 OK):**
```json
[
  {
    "timestamp": "2025-12-29T10:30:45.123Z",
    "playerAlias": "player999",
    "winAmount": 1100.00
  }
]
```

## Database Schema

### Jackpots Table
- `id` (UUID, primary key)
- `name` (VARCHAR)
- `win_probability` (DECIMAL)
- `current_size` (DECIMAL)
- `win_count` (INTEGER)
- `last_win_timestamp` (TIMESTAMP)
- `created_at` (TIMESTAMP)

### Bets Table
- `id` (UUID, primary key)
- `jackpot_id` (UUID, foreign key)
- `player_alias` (VARCHAR)
- `bet_amount` (DECIMAL)
- `created_at` (TIMESTAMP)

### Wins Table
- `id` (UUID, primary key)
- `jackpot_id` (UUID, foreign key)
- `player_alias` (VARCHAR)
- `win_amount` (DECIMAL)
- `timestamp` (TIMESTAMP)

## Implementation Details

- **Framework**: Spring Boot 3.3.4
- **Database**: PostgreSQL 16
- **ORM**: Hibernate/JPA
- **API Documentation**: OpenAPI 3.0 (Springdoc 2.6.0)
- **Build Tool**: Maven
- **Java Version**: 17

## Features Implemented

### Core Requirements ✅
- [x] Create jackpot with name and win probability
- [x] Get all jackpots with id, current size, win count, and last win timestamp
- [x] Place bets with random win determination
- [x] All bets persisted to database
- [x] Win recording with jackpot reset on win
- [x] List wins with timestamp, player alias, and win amount
- [x] Filtering and pagination support (limit/offset)
- [x] Single command deployment (`docker compose up`)

### Bonus Features ✅
- [x] OpenAPI/Swagger documentation
- [x] Health endpoint via Spring Actuator
- [x] Database persistence across container restarts (Docker volume)
- [x] Clean layered architecture (Controller → Service → Repository)
- [x] Exception handling with consistent error responses
- [x] DTOs for API contracts
- [x] Unit and integration tests (services + controllers), runnable with `mvn test`

## Production Considerations

**Security**: The project has some CVE warnings in transitive dependencies (detected by dependency scanners). For production deployment:
- Update to latest stable Spring Boot version
- Review and update all dependencies
- Enable authentication/authorization
- Use secrets management for database credentials

**Testing**: Unit and integration tests are included; run with `mvn test`.

## Project Structure

```
src/
├── main/
│   ├── java/com/example/jackpot/
│   │   ├── JackpotApplication.java
│   │   ├── controller/        (API endpoints)
│   │   ├── service/           (Business logic)
│   │   ├── repository/        (Data access)
│   │   ├── entity/            (JPA entities)
│   │   └── dto/               (Data transfer objects)
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/jackpot/
```

## Notes

- Bets are persisted to the database immediately
- Jackpots are reset to size 0 after a win
- Win records are maintained for historical tracking
- The service includes health checks and metrics via Spring Actuator
