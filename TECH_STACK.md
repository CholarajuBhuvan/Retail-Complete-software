# Technology Stack & Architecture

## 🏗️ Architecture Overview

**Architecture Pattern**: Monolithic MVC Application  
**Deployment Model**: Single JAR deployment with embedded Tomcat  
**Rendering**: Server-Side Rendering (SSR) with Thymeleaf  

## 🔧 Backend Technologies

### Core Framework
- **Spring Boot** 3.4.0
  - Rapid application development framework
  - Auto-configuration and dependency injection
  - Production-ready features (health checks, metrics)

### Spring Modules
- **Spring Web MVC** - Web application framework
- **Spring Security** 6.4.1 - Authentication and authorization
- **Spring Data JPA** 3.4.0 - Database abstraction layer
- **Spring DevTools** - Development hot-reload

### Data Layer
- **Hibernate** 6.6.3.Final (JPA Implementation)
  - ORM for database operations
  - Entity relationship mapping
  - Query optimization
- **H2 Database** 2.3.232
  - In-memory database for development
  - Zero configuration required
  - Fast startup and tear-down
- **Flyway** 10.20.1
  - Database version control
  - Schema migration management
  - Repeatable migrations

### Security
- **Spring Security**
  - Session-based authentication
  - Role-based access control (RBAC)
  - CSRF protection
  - Method-level security with `@PreAuthorize`
- **BCrypt Password Encoder**
  - Secure password hashing
  - Salt generation and verification

### Build Tool
- **Apache Maven** 3.13.0
  - Dependency management
  - Build lifecycle management
  - Multi-module project support
  - Maven Wrapper included (no installation required)

### Java Version
- **Java 21** (LTS)
  - Records for DTOs
  - Pattern matching
  - Virtual threads support (not yet utilized)

## 🎨 Frontend Technologies

### Template Engine
- **Thymeleaf** 3.1.3
  - Server-side HTML rendering
  - Spring Security integration
  - Natural templating (valid HTML)
  - Fragment reusability

### CSS Framework
- **Tailwind CSS** (via CDN)
  - Utility-first CSS framework
  - Responsive design
  - Component styling
  - Development: CDN for rapid prototyping
  - Production: Should be compiled/purged

### JavaScript Libraries
- **jQuery** 3.7.1
  - DOM manipulation
  - AJAX requests
  - Event handling
  - Simplified cross-browser compatibility

### Static Assets
- Served from `src/main/resources/static/`
- Auto-reload with Spring DevTools

## 🗄️ Database Design

### Database Type
- **H2 In-Memory Database**
  - Embedded mode
  - JVM process database
  - Resets on application restart

### ORM Configuration
```properties
Dialect: H2Dialect
DDL Strategy: Flyway-managed
Show SQL: false (production)
Format SQL: true (development)
```

### Entity Relationships
- **One-to-Many**: Customer → Orders, Order → OrderItems
- **Many-to-One**: OrderItem → Product, Order → Customer
- **Many-to-Many**: (Not currently implemented)

### Migration Strategy
- **Flyway** for version control
- Migration files: `src/main/resources/db/migration/`
- Naming: `V1__description.sql`, `V2__description.sql`

## 🔐 Security Architecture

### Authentication Flow
1. User submits credentials via `/login`
2. Spring Security validates against database
3. Session created with `JSESSIONID` cookie
4. User object stored in `SecurityContext`
5. Subsequent requests authenticated via session

### Authorization Levels
```
ROLE_MANAGER   → All features (dashboard, POS, products, inventory, customers, reports, users)
ROLE_ADMIN     → Products, Inventory
ROLE_CASHIER   → POS only
ROLE_EMPLOYEE  → Dashboard, POS, Customers
```

### Security Features
- Password encryption with BCrypt (strength 10)
- CSRF tokens on all POST/PUT/DELETE forms
- Session timeout configuration
- Role-based UI rendering (`sec:authorize`)
- Method-level security on controllers

## 📦 Key Dependencies

### Spring Boot Starters
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

### Database & Migration
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### Utilities
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

## ⚙️ Configuration Properties

### Server Configuration
```properties
server.port=8082
server.servlet.session.timeout=30m
server.error.whitelabel.enabled=true
```

### Database Configuration
```properties
spring.datasource.url=jdbc:h2:mem:billingsoftware
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
```

### Flyway Configuration
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true
```

### Thymeleaf Configuration
```properties
spring.thymeleaf.cache=false
spring.thymeleaf.enabled=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
```

### DevTools Configuration
```properties
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
spring.devtools.restart.exclude=static/**,public/**
```

### Application-Specific
```properties
app.tax.percent=10
```

## 🏛️ Design Patterns Used

### Repository Pattern
- Abstraction layer for data access
- Spring Data JPA repository interfaces
- Custom JPQL queries for complex operations

### Service Layer Pattern
- Business logic encapsulation
- Transaction management
- Service → Repository interaction

### MVC Pattern
- **Model**: Entity classes (domain package)
- **View**: Thymeleaf templates
- **Controller**: REST controllers (api package) & Web controllers (web package)

### DTO Pattern
- Java Records for data transfer
- Request/Response objects
- Prevents entity exposure

### Dependency Injection
- Constructor-based injection (recommended)
- Spring's IoC container
- Loose coupling between components

## 🚀 Performance Considerations

### Current Setup (Development)
- In-memory database for fast access
- DevTools hot-reload (automatic restart)
- No caching implemented
- Direct database queries (no query optimization)

### Production Recommendations
- Switch to persistent database (PostgreSQL/MySQL)
- Implement Redis for session storage
- Enable second-level Hibernate cache
- Compile and minify Tailwind CSS
- Add CDN for static assets
- Enable GZIP compression
- Connection pooling (HikariCP - included by default)

## 🧪 Testing Stack (Not Yet Implemented)

### Recommended Tools
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **Testcontainers** - Database testing with containers
- **RestAssured** - API testing

## 📊 Monitoring & Logging

### Current Setup
- Console logging (SLF4J with Logback)
- System.out debugging (to be replaced)
- Exception stack traces

### Production Recommendations
- **Spring Boot Actuator** - Health checks, metrics
- **Micrometer** - Application metrics
- **ELK Stack** - Centralized logging (Elasticsearch, Logstash, Kibana)
- **Prometheus + Grafana** - Monitoring dashboards

## 🔄 Development Workflow

### Hot Reload Mechanism
1. Code change detected by DevTools
2. Application context restarted
3. Static resources served directly (no restart)
4. Browser auto-refresh with LiveReload

### Build Process
```bash
mvnw clean                  # Clean target directory
mvnw compile                # Compile Java sources
mvnw test                   # Run tests (when implemented)
mvnw package                # Create JAR file
mvnw spring-boot:run        # Run application
```

### Database Migration Workflow
1. Create SQL file: `V{version}__{description}.sql`
2. Place in `src/main/resources/db/migration/`
3. Restart application
4. Flyway automatically applies migration

## 📈 Scalability Considerations

### Current Limitations
- Single-instance deployment (no clustering)
- In-memory session storage (not cluster-friendly)
- H2 database (single connection, in-memory)
- No load balancing support

### Scaling Strategy
1. **Horizontal Scaling**: Add Redis for session store
2. **Database**: Move to PostgreSQL with connection pool
3. **Caching**: Implement Redis/Hazelcast
4. **Load Balancer**: Nginx/HAProxy in front
5. **Containerization**: Docker + Kubernetes
6. **CDN**: CloudFlare for static assets

## 🔒 Security Best Practices Implemented

✅ Password hashing with BCrypt  
✅ CSRF protection on state-changing operations  
✅ Session-based authentication  
✅ Role-based access control  
✅ SQL injection prevention (JPA/Hibernate)  
✅ XSS prevention (Thymeleaf auto-escaping)  

### Security Enhancements Recommended
- HTTPS/TLS in production
- Rate limiting on login attempts
- OAuth2/JWT for API authentication
- Security headers (HSTS, CSP, X-Frame-Options)
- Input validation with Bean Validation
- Audit logging for sensitive operations

## 📝 Code Quality & Standards

### Java Conventions
- Package naming: `in.bhuvan.billingsoftware.*`
- Camel case for methods/variables
- Pascal case for classes
- Constants in UPPER_SNAKE_CASE

### Lombok Annotations Used
- `@RequiredArgsConstructor` - Constructor injection
- `@Data` - Getters, setters, toString (entities)
- `@SuppressWarnings` - Null safety warnings

### Spring Annotations
- `@RestController` - REST API endpoints
- `@Service` - Business logic layer
- `@Repository` - Data access layer
- `@Component` - Generic Spring beans
- `@Configuration` - Configuration classes
- `@PreAuthorize` - Method-level security

## 🌐 API Design

### RESTful Principles
- HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete)
- Resource-based URLs: `/api/products/{id}`
- JSON request/response bodies
- HTTP status codes: 200 (OK), 201 (Created), 404 (Not Found), etc.

### Response Format
```json
{
  "id": 1,
  "name": "Product Name",
  "price": 99.99,
  "stockQuantity": 50
}
```

### Error Handling
- Exception handling with `@ControllerAdvice` (to be improved)
- Error responses with meaningful messages
- HTTP status codes for error types

---

**Last Updated**: November 16, 2025  
**Spring Boot Version**: 3.4.0  
**Java Version**: 21  
**Maven Version**: 3.13.0
