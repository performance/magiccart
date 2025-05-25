# MagicCart - Full-Stack Kotlin Discount Negotiation Platform

A sophisticated full-stack Kotlin application implementing a discount negotiation and bidding platform for e-commerce, featuring a Spring Boot backend, Kotlin Multiplatform shared types, and a Kotlin/JS Chrome extension.

## 🏗️ Monorepo Structure

```
magiccart/
├── backend/          # Spring Boot 3.4.5 Kotlin backend
├── shared/           # Kotlin Multiplatform shared types (JVM + JS)
├── extension/        # Kotlin/JS Chrome extension
├── build.gradle.kts  # Root build configuration
└── settings.gradle.kts
```

## 🚀 Quick Start

### Prerequisites
- Java 21
- PostgreSQL 15+ 
- Node.js 18+ (for Kotlin/JS compilation)
- Gradle (included via wrapper)

### Database Setup
1. Install and start PostgreSQL
2. Create database:
   ```sql
   CREATE DATABASE magiccart;
   ```
3. Update connection details in `backend/src/main/resources/application.yml` if needed

### Building the Project
```bash
# Build all modules (backend, shared, extension)
./gradlew build

# Build specific modules
./gradlew :backend:build
./gradlew :shared:build  
./gradlew :extension:build
```

### Running the Backend
```bash
# Run the Spring Boot backend
./gradlew :backend:bootRun
```

The backend will:
- Start on http://localhost:8080
- Automatically create database schema via `schema.sql`
- Load sample data via `data.sql`
- Serve API documentation at http://localhost:8080/swagger-ui.html
- Serve test UI at http://localhost:8080/

### Building the Chrome Extension
```bash
# Build the extension
./gradlew :extension:build

# The compiled extension will be in:
# extension/build/dist/js/productionExecutable/
```

## 🎯 API Endpoints
- `GET /api/bidding-rules?productId={id}` - Fetch bidding rules and offers
- `POST /api/validate-selection` - Validate vendor selection and get checkout instructions

## 🛠️ Technology Stack

### Backend (`/backend`)
- **Kotlin** with Spring Boot 3.4.5
- **Spring Data JDBC** with PostgreSQL JSONB support
- **SpringDoc OpenAPI** for API documentation
- **Spring Security** (configured for development)

### Shared Module (`/shared`)
- **Kotlin Multiplatform** (JVM + JS targets)
- **kotlinx.serialization** for JSON handling
- Shared DTOs, enums, and data types

### Chrome Extension (`/extension`) 
- **Kotlin/JS** targeting Chrome Extension Manifest V3
- **kotlin-react** for UI components
- **kotlinx.coroutines** for async operations
- Content script injection on supported e-commerce sites

## 📁 Project Architecture

### Backend Structure
- `config/` - Spring configuration (Jackson, JDBC, Security, Error handling)
- `domain/model/` - JPA entities  
- `domain/repository/` - Spring Data JDBC repositories
- `domain/converter/` - PostgreSQL JSONB converters
- `service/` - Business logic services
- `web/` - REST controllers
- `dto/` - API data transfer objects (now using shared module)

### Shared Module Structure  
- `commonMain/kotlin/` - Cross-platform DTOs and types
- `jvmMain/kotlin/` - JVM-specific implementations
- `jsMain/kotlin/` - JS-specific implementations

### Extension Structure
- `ContentScript.kt` - Injects bidding overlay on product pages
- `ServiceWorker.kt` - Background script for API calls and storage
- `ui/` - React components for the bidding interface

## 🧪 Testing
```bash
# Run backend tests (requires PostgreSQL)
./gradlew :backend:test

# Build without tests
./gradlew build -x test
```

## 🔧 Development

### Adding New Shared Types
1. Add DTOs/enums to `shared/src/commonMain/kotlin/`
2. Use `@Serializable` annotation for JSON support
3. Both backend and extension automatically get access

### Backend Development
- Uses shared types from `:shared` module
- Follow existing Spring Boot patterns
- Use proper error handling via `GlobalExceptionHandler`

### Extension Development  
- Kotlin/JS with React for UI
- Service Worker for API communication
- Content scripts for DOM manipulation on e-commerce sites

## 📈 Next Steps - Phase 2.A: Extension Skeleton

Ready to implement:
1. **Enhanced Product Detection** - Amazon, Best Buy, etc.
2. **API Integration** - Connect extension to backend via Service Worker
3. **Basic React UI** - Product info display and bidding controls
4. **Chrome Storage** - Persist bidding sessions

The foundation is set for a powerful full-stack Kotlin discount negotiation platform! 🛒✨