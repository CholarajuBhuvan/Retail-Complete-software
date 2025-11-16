# Billing Software - Full Stack Application

A comprehensive Point of Sale (POS) and inventory management system built with Spring Boot and Thymeleaf.

## 🚀 Quick Start

### Prerequisites
- **Java 21** or higher
- **Maven 3.6+** (included via Maven Wrapper)
- **Windows PowerShell** or any terminal

### Running the Application

**Option 1: Using PowerShell Script (Recommended)**
```powershell
cd "C:\Users\Cholaraju Bhuvan\OneDrive\Desktop\software bhuvan"
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

**Option 2: Manual Start**
```powershell
cd "C:\Users\Cholaraju Bhuvan\OneDrive\Desktop\software bhuvan\billingsoftware"
.\mvnw.cmd spring-boot:run
```

The application will start on **http://localhost:8082**

### Default Login Credentials

| Role     | Username | Password    | Access Level                              |
|----------|----------|-------------|-------------------------------------------|
| Manager  | manager  | manager123  | Full access to all features               |
| Admin    | admin    | admin123    | Products & Inventory management           |
| Cashier  | cashier  | cashier123  | POS only                                  |
| Employee | employee | employee123 | Dashboard, POS, Customers                 |

## 📋 Features

### Core Modules
- **Point of Sale (POS)** - Fast checkout with product search, cart management, and receipt generation
- **Inventory Management** - Real-time stock tracking with low-stock alerts
- **Product Management** - Complete CRUD operations with SKU, pricing, and stock control
- **Customer Management** - Customer database with purchase history
- **Sales Reports** - Date-range based sales analytics with revenue summaries
- **User Management** - Role-based access control (Manager can create Admin/Cashier accounts)

### Security Features
- **Role-Based Access Control**
  - CASHIER: POS only
  - ADMIN: Products & Inventory only
  - MANAGER: All features including user management
  - EMPLOYEE: Dashboard, POS, Customers
- **Spring Security** with session-based authentication
- **CSRF Protection** on all forms
- **BCrypt Password Encryption**

### Technical Features
- Server-side rendering with **Thymeleaf**
- **H2 In-Memory Database** (auto-resets on restart)
- **Flyway Database Migrations** for schema versioning
- **Spring DevTools** for hot-reload during development
- **Tailwind CSS** for responsive UI
- **jQuery/AJAX** for dynamic frontend interactions

## 🗂️ Application Structure

```
billingsoftware/
├── src/main/java/in/bhuvan/billingsoftware/
│   ├── api/          # REST API Controllers
│   ├── web/          # Web Page Controllers
│   ├── domain/       # Entity Models
│   ├── repo/         # JPA Repositories
│   ├── service/      # Business Logic Layer
│   ├── config/       # Security & App Configuration
│   └── bootstrap/    # Data Initialization
├── src/main/resources/
│   ├── templates/    # Thymeleaf HTML templates
│   ├── static/       # CSS, JS, Images
│   ├── db/migration/ # Flyway SQL migrations
│   └── application.properties
└── pom.xml          # Maven dependencies
```

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Products
- `GET /api/products` - List all products
- `GET /api/products?q={search}` - Search products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Customers
- `GET /api/customers` - List all customers
- `GET /api/customers?q={search}` - Search customers
- `POST /api/customers` - Create customer

### Orders
- `POST /api/orders` - Create order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders` - List all orders

### Reports
- `GET /api/reports/sales?from={date}&to={date}` - Sales report (Manager/Admin only)
- `GET /api/reports/low-stock?threshold={number}` - Low stock report

### Users (Manager only)
- `GET /api/users` - List all users
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `PUT /api/users/{id}/password` - Reset password
- `PATCH /api/users/{id}/status` - Enable/disable user

## 🌐 Web Pages

- `/login` - Login page
- `/dashboard` - Dashboard with overview (Manager/Employee)
- `/pos` - Point of Sale interface (Cashier/Manager/Employee)
- `/products` - Product management (Admin/Manager)
- `/inventory` - Inventory management (Admin/Manager)
- `/customers` - Customer management (Manager/Employee)
- `/reports` - Sales reports (Manager only)
- `/users` - User management (Manager only)

## 📊 Database Schema

The application uses **11 database tables**:
- `users` - User accounts with roles
- `customers` - Customer information
- `products` - Product catalog
- `categories` - Product categories
- `suppliers` - Supplier information
- `orders` - Customer orders
- `order_items` - Order line items
- `payments` - Payment transactions
- `inventory_transactions` - Stock movement tracking
- `shifts` - Employee shift management
- `discounts` - Discount/promotion management

## 🛠️ Configuration

### Application Properties
Located in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8082

# Database (H2 In-Memory)
spring.datasource.url=jdbc:h2:mem:billingsoftware
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# DevTools (Hot Reload)
spring.devtools.restart.enabled=true

# Thymeleaf
spring.thymeleaf.cache=false
```

## 🧪 Testing

Create a test order via API:
```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1}
    ],
    "discountPercent": 5
  }'
```

## 🐛 Troubleshooting

### Port Already in Use
```powershell
# Find process using port 8082
netstat -ano | findstr :8082

# Kill the process
taskkill /PID <PID> /F
```

### Clean Build
```powershell
cd billingsoftware
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

### Database Issues
The H2 database is in-memory and resets on every restart. Sample data is automatically seeded on startup.

### Access H2 Console (Development)
Enable in `application.properties`:
```properties
spring.h2.console.enabled=true
```
Access at: http://localhost:8082/h2-console

## 📝 Notes

- **Data Persistence**: Using H2 in-memory database - all data is lost on restart
- **Auto-reload**: Spring DevTools automatically restarts on code changes
- **Sample Data**: 4 users and 5 products are seeded on startup
- **Security**: All endpoints except login require authentication
- **Tax Rate**: Default 10% (configurable in `application.properties` via `app.tax.percent`)

## 📞 Support

For issues or questions, check the application logs in the terminal where Spring Boot is running.
