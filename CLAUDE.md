# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MagicCart is a sophisticated Spring Boot 3.4+ Kotlin application that implements a **discount negotiation and bidding platform** for e-commerce. The platform allows customers to negotiate better prices across multiple vendors through a rule-based bidding system.

## Technology Stack

- **Language**: Kotlin
- **Framework**: Spring Boot 3.4.5
- **Database**: PostgreSQL with JSONB support
- **Build Tool**: Gradle (Kotlin DSL)
- **Java Version**: 21
- **Key Dependencies**: Spring Data JDBC, Spring Security, SpringDoc OpenAPI, Jackson

## Common Development Commands

```bash
# Build the application
./gradlew build

# Run the application (requires PostgreSQL)
./gradlew bootRun

# Run tests
./gradlew test

# Check dependencies
./gradlew dependencies

# Clean build artifacts
./gradlew clean
```

## Database Setup

The application requires PostgreSQL with a database named `magiccart`. Default connection:
- URL: `jdbc:postgresql://localhost:5432/magiccart`
- Username: `postgres`
- Password: `password`

Schema and sample data are automatically loaded via `schema.sql` and `data.sql`.

## Architecture

### Core Domain Structure

**Entities**:
- `Product` - Catalog items with JSONB specifications
- `Vendor` - Sellers with integration levels and rating systems
- `VendorOffer` - Real-time pricing from vendors
- `VendorRule` - JSONB-based dynamic pricing rules with trigger conditions
- `VendorRuleProduct` - Product-specific rule associations

**Key Patterns**:
- **JSONB Integration**: PostgreSQL JSONB fields store complex rule structures and product specifications
- **Rule Engine**: Flexible vendor pricing rules with trigger conditions and counter actions
- **Multi-vendor Support**: Different integration levels (DEEP_API, AFFILIATE_PARAMS, COUPON_CODES, ASSISTED)

### API Endpoints

- `GET /api/bidding-rules?productId={id}` - Fetch available offers and vendor rules for a product
- `POST /api/validate-selection` - Process user's final vendor selection and generate checkout instructions

### JSONB Converters

Custom Spring Data JDBC converters handle complex JSONB mappings:
- `TriggerCondition` ↔ PostgreSQL JSONB
- `CounterAction` ↔ PostgreSQL JSONB  
- `Map<String, Boolean>` ↔ PostgreSQL JSONB
- Product specifications (String) ↔ PostgreSQL JSONB

## Key Configuration Classes

- `JdbcConvertersConfig` - Registers JSONB converters with Spring Data JDBC
- `JacksonConfig` - Configures JSON serialization with Kotlin support
- `SecurityConfig` - Permits API access for testing (disable CSRF)

## Testing and Documentation

- **API Documentation**: Available at `/swagger-ui.html` when running
- **Test Data**: Comprehensive sample data in `data.sql` for immediate testing
- **Integration Tests**: Use `@SpringBootTest` for full context testing

## Development Notes

- PostgreSQL JSONB is central to the rule engine - ensure proper indexing for performance
- Vendor rules support multiple applicability types: VENDOR_WIDE, CATEGORY, SPECIFIC_PRODUCTS, BUNDLE
- Checkout integration is currently limited to ASSISTED method for MVP
- All monetary calculations use `BigDecimal` for precision
- Extensive logging configured for debugging rule engine behavior