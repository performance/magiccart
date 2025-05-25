# MagicCart - Discount Negotiation Platform

A sophisticated Spring Boot 3.4+ Kotlin application implementing a discount negotiation and bidding platform for e-commerce.

## Quick Start

### Prerequisites
- Java 21
- PostgreSQL 15+ 
- Gradle (included via wrapper)

### Database Setup
1. Install and start PostgreSQL
2. Create database:
   ```sql
   CREATE DATABASE magiccart;
   ```
3. Update connection details in `src/main/resources/application.yml` if needed

### Running the Application
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will:
- Start on http://localhost:8080
- Automatically create database schema via `schema.sql`
- Load sample data via `data.sql`
- Serve API documentation at http://localhost:8080/swagger-ui.html

### API Endpoints
- `GET /api/bidding-rules?productId={id}` - Fetch bidding rules and offers
- `POST /api/validate-selection` - Validate vendor selection and get checkout instructions

### Technology Stack
- **Kotlin** with Spring Boot 3.4.5
- **Spring Data JDBC** with PostgreSQL JSONB support
- **SpringDoc OpenAPI** for API documentation
- **Spring Security** (currently permissive for testing)

## Project Structure
- `domain/model/` - Entities and enums
- `domain/repository/` - Data access layer
- `domain/converter/` - JSONB converters
- `service/` - Business logic
- `web/` - REST controllers
- `config/` - Spring configuration

## Testing
```bash
# Run tests (requires PostgreSQL)
./gradlew test

# Build without tests
./gradlew build -x test
```