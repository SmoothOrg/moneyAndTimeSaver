# Backend Setup Guide

## Prerequisites

1. **Java 17** - Installed and configured
2. **Maven 3.8+** - For building the project
3. **PostgreSQL 14+** - Database server
4. **Elasticsearch 8.x** - Search engine (already configured)

---

## Step-by-Step Setup

### 1. Install PostgreSQL

#### macOS (using Homebrew):
```bash
brew install postgresql@14
brew services start postgresql@14
```

#### Ubuntu/Debian:
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

#### Windows:
Download installer from: https://www.postgresql.org/download/windows/

### 2. Create Database

```bash
# Connect to PostgreSQL
psql postgres

# Create database
CREATE DATABASE money_time_saver;

# Create user (optional, or use default postgres user)
CREATE USER money_saver_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE money_time_saver TO money_saver_user;

# Exit psql
\q
```

Or run the SQL script:
```bash
psql -U postgres -f database_setup.sql
```

### 3. Update Database Configuration

If you're NOT using default postgres/postgres credentials, update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/money_time_saver
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Build the Project

```bash
cd /Users/Akash.Verma/IntellijProjects/moneyAndTimeSaver

# Clean and build
./mvnw clean install

# Or in IntelliJ: Maven → Reload Project
```

### 5. Run the Application

```bash
# Run from command line
./mvnw spring-boot:run -pl web

# Or in IntelliJ: Run MoneyAndTimeSaverApplication
```

The server will start on **http://localhost:8080**

---

## Testing the APIs

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "akash@example.com",
    "password": "password123",
    "name": "Akash Verma",
    "latitude": 28.5687,
    "longitude": 77.1886
  }'
```

**Response:**
```json
{
  "message": "User registered successfully!"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "akash@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "id": 1,
  "email": "akash@example.com",
  "name": "Akash Verma",
  "defaultLatitude": 28.5687,
  "defaultLongitude": 77.1886
}
```

**Save the token** - you'll need it for authenticated requests!

### 3. Search Products (No Auth Required)

```bash
curl "http://localhost:8080/api/products/search/by-location?query=paneer&latitude=28.5687&longitude=77.1886&size=5"
```

### 4. Get Current User Info (Requires Auth)

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Add Item to Cart (Requires Auth)

```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productData": {
      "product_id": "7HYSGXOZ4T",
      "product_name": "Sundrop Peanut Butter",
      "brand_name": "Sundrop",
      "platforms": [
        {
          "platform": "blinkit",
          "selling_price": 135,
          "availability": true
        }
      ]
    },
    "quantity": 1
  }'
```

### 6. Get Cart Items

```bash
curl -X GET http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Calculate Cart with Platform Fees

```bash
curl -X GET http://localhost:8080/api/cart/calculate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response will show platform-wise pricing with delivery fees!**

---

## API Endpoints Summary

### Public Endpoints (No Auth)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/products/search` - Basic product search
- `GET /api/products/search/by-location` - Location-based search
- `GET /api/products/search/by-geohash` - Geohash-based search

### Protected Endpoints (Requires JWT Token)
- `GET /api/auth/me` - Get current user info
- `PUT /api/auth/location` - Update default location

#### Cart Management
- `POST /api/cart/add` - Add item to cart
- `GET /api/cart` - Get cart items
- `GET /api/cart/calculate` - Calculate with platform fees
- `PUT /api/cart/{id}/quantity` - Update quantity
- `DELETE /api/cart/{id}` - Remove item
- `DELETE /api/cart/clear` - Clear cart

---

## Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
brew services list  # macOS
sudo systemctl status postgresql  # Linux

# Test connection
psql -U postgres -d money_time_saver
```

### Port Already in Use

If port 8080 is taken, change in `application.properties`:
```properties
server.port=8081
```

### JWT Token Errors

- Make sure you're sending: `Authorization: Bearer YOUR_TOKEN`
- Tokens expire after 24 hours (configurable in `application.properties`)

### Elasticsearch Connection

Make sure Elasticsearch is running:
```bash
curl http://localhost:9200
```

---

## Configuration Files

### Key Properties

**application.properties**
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/money_time_saver
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT
app.jwt.secret=YOUR_SECRET_KEY
app.jwt.expiration-ms=86400000  # 24 hours

# Elasticsearch
elasticsearch.active=local
elasticsearch.local.host=localhost
elasticsearch.local.port=9200
```

**platform-fees.properties**
- Update delivery fees when you get actual data
- Currently using manual fallback values

---

## Next Steps

1. ✅ Backend is ready!
2. 🔄 **Frontend Integration** - Connect React app to these APIs
3. 🔄 **UI Improvements** - Modernize the interface
4. 📊 **Analytics** - Add price tracking, history

---

## Support

If you encounter issues:
1. Check logs: `tail -f logs/spring.log`
2. Verify database: `psql -U postgres -d money_time_saver`
3. Test individual endpoints with curl
