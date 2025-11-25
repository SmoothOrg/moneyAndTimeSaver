# 💰⏰ MoneyAndTimeSaver - Quick Commerce Price Comparison Platform

> Compare prices across Blinkit, Zepto & Swiggy Instamart. Find the best deals with our smart multi-platform optimizer!

---

## 🎯 What It Does

**MoneyAndTimeSaver** helps users find the cheapest way to buy groceries from quick-commerce platforms by:
- Comparing prices across Blinkit, Zepto, and Swiggy Instamart
- Finding the absolute best combination (even across multiple platforms)
- Showing complete cost breakdowns including all delivery and handling fees
- Providing location-based product availability
- Tracking user preferences and locations

---

## ✨ Key Features

### 🔍 Smart Search
- Location-based product search using geohash
- Real-time autocomplete suggestions
- Fuzzy matching for typo tolerance
- Multi-field search (name, brand, category)

### 🛒 Intelligent Cart
- **4 Comparison Options:**
  1. 🎯 Best Combo (Global Optimizer) - Absolute cheapest
  2. Blinkit Primary - Prefer Blinkit with fallbacks
  3. Zepto Primary - Prefer Zepto with fallbacks
  4. Swiggy Primary - Prefer Swiggy with fallbacks
- Complete fee transparency
- Multi-platform delivery fee breakdown
- Free delivery detection per platform
- Handles 2, 3, or more platform combinations

### 📍 Location System
- Dual tracking: Current (session) + Default (home)
- GPS detection with "Save as default" option
- Pincode-based location lookup
- sessionStorage for temporary location
- Database persistence for analytics

### 🔐 Security
- JWT-based authentication
- BCrypt password encryption
- Protected routes
- Token expiration (24 hours)
- Stateless session management

---

## 🏗️ Architecture

### Tech Stack

**Backend:**
- Spring Boot 3.5.3
- Spring Security + JWT
- PostgreSQL (User management)
- Elasticsearch (Product search)
- Maven multi-module

**Frontend:**
- React 18 + Vite
- Material-UI
- React Router
- Context API (State management)

**External Services:**
- Nominatim (Reverse geocoding)
- Postal PIN API (Pincode lookup)

---

## 📂 Project Structure

```
moneyAndTimeSaver/ (Backend)
├── domain/          # Entities & Repositories
│   ├── entity/      # User, CartItem, UserLocation
│   └── repository/  # JPA repositories
├── services/        # Business Logic
│   ├── auth/        # Authentication service
│   ├── security/    # JWT, UserDetails
│   ├── elastic/     # Elasticsearch service
│   ├── cart/        # Cart service
│   └── util/        # GeohashUtils
├── ingestion/       # Data ingestion (future)
└── web/             # API Layer
    ├── controller/  # REST controllers
    ├── security/    # Security config
    ├── config/      # App configuration
    ├── dto/         # Request/Response objects
    └── resources/   # application.properties

RealTimeCompare/ (Frontend)
├── src/
│   ├── components/  # Reusable components
│   ├── context/     # AuthContext, CartContext
│   ├── pages/       # Login, Home, Cart, Search
│   ├── services/    # API integration
│   └── images/      # Platform logos
└── public/
```

---

## 🚀 Getting Started

### Prerequisites
```bash
# Java 17+
java -version

# Maven
mvn -version

# Node.js 18+
node -version

# PostgreSQL
psql --version

# Elasticsearch 7.17+
curl http://localhost:9200
```

### 1. Setup Database

```bash
# PostgreSQL
psql -U Akash.Verma postgres
CREATE DATABASE money_time_saver;
\q

# Elasticsearch - Create index
# See: ingestion.txt for mapping
```

### 2. Add Sample Data

```bash
# Open Kibana Dev Tools: http://localhost:5601/app/dev_tools#/console
# Run scripts from:
# - sample_data_faridabad.txt (basic products)
# - ADD_3_PLATFORM_DATA.txt (3-platform test)
# - EXTREME_PRICE_DIFFERENCE_DATA.txt (extreme scenario)
```

### 3. Start Backend

```bash
cd moneyAndTimeSaver

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run -pl web

# Runs on: http://localhost:8080
```

### 4. Start Frontend

```bash
cd RealTimeCompare

# Install dependencies (first time)
npm install

# Run development server
npm run dev

# Opens at: http://localhost:5173
```

---

## 🧪 Testing

### Quick Test Flow:

1. **Register**: Create account at http://localhost:5173/register
2. **Set Location**: Allow GPS or enter pincode (121002)
3. **Search**: Try "paneer", "milk", "bread"
4. **Add to Cart**: Select multiple products
5. **Compare**: View cart to see 4 comparison options
6. **Best Deal**: Algorithm picks cheapest option!

### Test Data Locations:
- **Geohash**: ttncyvn (7-character)
- **Coordinates**: 28.4614, 77.2981
- **City**: Faridabad, Haryana
- **Pincode**: 121002

---

## 📊 Sample Results

### Example Cart Analysis:
```
Cart Items:
- iPhone Cable (₹299 on Blinkit vs ₹1799 on Zepto)
- Lindt Chocolate (₹199 on Zepto vs ₹420 on Blinkit)
- Olive Oil (₹449 on Swiggy vs ₹699 on Blinkit)

Results:
1. 🎯 Best Combo (3-Platform): ₹972 🏆
   - Saves ₹1752 compared to Zepto!
   - Uses all 3 platforms optimally
   
2. Blinkit: ₹1426
3. Zepto: ₹2724
4. Swiggy: ₹2750
```

---

## 🔑 API Endpoints

### Authentication (Public)
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login and get JWT

### Products (Public)
- `GET /api/products/search` - Text search
- `GET /api/products/search/by-location` - Location-based search

### User (Protected)
- `GET /api/user/me` - Get user profile
- `PUT /api/user/location/default` - Update home location
- `PUT /api/user/location/current` - Update browsing location

### Cart (Protected)
- `POST /api/cart/calculate` - Calculate best platform deals

---

## ⚙️ Configuration

### Backend (application.properties)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/money_time_saver
spring.datasource.username=Akash.Verma

# Elasticsearch
elasticsearch.local.host=localhost
elasticsearch.local.port=9200

# JWT
app.jwt.secret=<your-secret>
app.jwt.expiration-ms=86400000
```

### Platform Fees (platform-fees.properties)
```properties
platforms.blinkit.delivery-fee=25
platforms.blinkit.free-delivery-threshold=99
platforms.zepto.delivery-fee=20
platforms.swiggy_instamart.delivery-fee=30
platforms.swiggy_instamart.free-delivery-threshold=149
```

---

## 📈 Analytics Capabilities

### User Behavior:
- Track user locations (current vs default)
- Monitor location changes
- Identify traveling users
- City-wise user distribution

### Product Analytics:
- Search patterns by location
- Popular products per area
- Price sensitivity analysis
- Platform preference tracking

### Business Insights:
- Optimal pricing strategies
- Delivery fee impact on choices
- Multi-platform ordering patterns
- Combo usage statistics

---

## 🔒 Security

- JWT tokens (HS256 algorithm)
- BCrypt password hashing
- CORS protection
- Protected routes
- User-specific data isolation
- No session cookies (stateless)

---

## 🚧 Future Enhancements

### High Priority:
- [ ] Real-time inventory sync with platforms
- [ ] Price history tracking
- [ ] Price drop alerts
- [ ] User preferences (favorite products)
- [ ] Order history

### Medium Priority:
- [ ] External API for platform fees
- [ ] Push notifications
- [ ] Social sharing
- [ ] Product reviews
- [ ] Mobile app (React Native)

### Low Priority:
- [ ] Admin dashboard
- [ ] Analytics UI
- [ ] Multi-city support
- [ ] Referral system
- [ ] Dark mode

---

## 📚 Documentation

- `BACKEND_COMMIT_SUMMARY.md` - Complete backend changes
- `FRONTEND_COMMIT_SUMMARY.md` - Complete frontend changes
- `AUTH_API.md` - Authentication API docs
- `IMPLEMENTATION_PLAN.md` - Technical architecture
- `PROJECT_COMPLETE.md` - Feature overview
- `TESTING_GUIDE.md` - Setup and testing
- `LOCATION_SYSTEM_COMPLETE.md` - Location feature details
- `CART_COMPLETE.md` - Cart functionality
- `GEOHASH_FIX.md` - Geohash precision guide

---

## 🐛 Known Issues

- None! All major features working ✅

---

## 🤝 Contributing

This is currently a personal project. For questions or suggestions, contact the developer.

---

## 👨‍💻 Developer

**Akash Verma**
- Email: avash0208@gmail.com
- Built with assistance from Claude (Anthropic)

---

## 📄 License

Proprietary - All Rights Reserved

---

## 🎉 Acknowledgments

- Spring Boot team for excellent framework
- Elasticsearch for powerful search
- Material-UI for beautiful components
- Anthropic's Claude for development assistance

---

## 🚀 Quick Commands

### Backend:
```bash
cd moneyAndTimeSaver
./mvnw spring-boot:run -pl web
```

### Frontend:
```bash
cd RealTimeCompare
npm run dev
```

### Test:
```
http://localhost:5173
```

---

**Happy Saving! 💰⏰**
