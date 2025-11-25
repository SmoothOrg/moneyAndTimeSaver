# Money And Time Saver - Implementation Plan

## Project Overview
A price comparison platform for quick-commerce groceries (Blinkit, Zepto, Swiggy Instamart) with location-based search using Elasticsearch and Spring Boot backend with React frontend.

---

## Backend Changes Completed ✅

### 1. Geohash Utility
- **File**: `services/src/main/java/com/smoothOrg/services/util/GeohashUtils.java`
- **Purpose**: Convert lat/long to geohash for location-based filtering
- **Usage**: `GeohashUtils.encode(latitude, longitude, precision)`

### 2. Enhanced Product Search Endpoints
- **File**: `web/src/main/java/com/smoothOrg/web/controller/ProductSearchController.java`
- **New Endpoint**: `GET /api/products/search/by-location`
  - Params: `query`, `latitude`, `longitude`, `precision` (default 9), `size`, `index`
  - Automatically converts location to geohash
  - Returns products available at that location

### 3. Cart Calculation with Platform Fees
- **Files**: 
  - `web/src/main/resources/platform-fees.properties` (fee configuration)
  - `web/src/main/java/com/smoothOrg/web/config/PlatformFeesConfig.java`
  - `web/src/main/java/com/smoothOrg/web/controller/CartController.java`
- **Endpoint**: `POST /api/cart/calculate`
- **Features**:
  - Calculates platform-wise totals
  - Includes delivery fees (free above threshold)
  - Adds handling and platform fees
  - **Smart combo logic**: Finds cheapest alternatives when items unavailable
  - **Shows combo even when all available**: Compares all platforms
  - Sorts results by total cost

### 4. CORS Configuration
- **File**: `web/src/main/java/com/smoothOrg/web/config/WebConfig.java`
- Allows frontend (localhost:5173) to call backend APIs

---

## Platform Fee Configuration

### Current Settings (in platform-fees.properties):
```
Platform         | Delivery Fee | Free Delivery Threshold | Handling | Platform Fee
-----------------|--------------|------------------------|----------|-------------
Blinkit          | ₹25         | ₹99                    | ₹5       | ₹3
Zepto            | ₹20         | ₹99                    | ₹4       | ₹2
Swiggy Instamart | ₹30         | ₹149                   | ₹6       | ₹5
```

**Note**: These are configurable. Update `platform-fees.properties` as actual fees change.

---

## API Endpoints Summary

### Product Search
1. **Basic Search**
   ```
   GET /api/products/search?query=paneer&size=10
   ```

2. **Location-based Search** (Recommended for frontend)
   ```
   GET /api/products/search/by-location?query=milk&latitude=28.5687&longitude=77.1886&size=10
   ```

3. **Geohash Search** (if geohash already calculated)
   ```
   GET /api/products/search/by-geohash?query=bread&geohash=ttnt7u5p9&size=10
   ```

### Cart Calculation
```
POST /api/cart/calculate
Content-Type: application/json

{
  "items": [
    {
      "product_name": "Amul Paneer",
      "platforms": [
        { "platform": "blinkit", "selling_price": 75, "availability": true },
        { "platform": "zepto", "selling_price": 80, "availability": false },
        { "platform": "swiggy_instamart", "selling_price": 78, "availability": true }
      ]
    }
  ]
}
```

**Response includes**:
- Subtotal per platform
- Delivery fee (or "Free Delivery")
- Handling & platform fees
- Total cost
- Items with fallback platforms if unavailable

---

## Frontend Integration Required

### 1. Replace Dummy Data with API Calls

**Current**: `src/data/dummyData.js`
**Replace with**: API service

```javascript
// src/services/api.js
const API_BASE = 'http://localhost:8080/api';

export const searchProducts = async (query, latitude, longitude) => {
  const response = await fetch(
    `${API_BASE}/products/search/by-location?` +
    `query=${encodeURIComponent(query)}` +
    `&latitude=${latitude}&longitude=${longitude}&size=20`
  );
  return response.json();
};

export const calculateCart = async (cartItems) => {
  const response = await fetch(`${API_BASE}/cart/calculate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ items: cartItems })
  });
  return response.json();
};
```

### 2. Update SearchResults.jsx
- Call `searchProducts()` instead of using `dummyData`
- Use actual location from `LocationComponent`

### 3. Update Cart.jsx
- Call `calculateCart()` API
- Display delivery fees and smart combos from backend response

---

## Database Schema (PostgreSQL Recommended)

### Why PostgreSQL?
- ✅ **Cost**: Free (self-hosted) or cheap (AWS RDS Free Tier)
- ✅ **Simplicity**: Native Spring Boot JPA support
- ✅ **JSONB**: Flexible for cart items while maintaining relational integrity
- ✅ **Future-proof**: Easy to add analytics, price history, orders

### Schema
```sql
-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    default_latitude DECIMAL(10, 8),
    default_longitude DECIMAL(11, 8),
    default_geohash VARCHAR(12),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cart (JSONB for flexibility)
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    product_data JSONB NOT NULL,
    quantity INTEGER DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Saved Locations
CREATE TABLE user_locations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    name VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    geohash VARCHAR(12),
    is_default BOOLEAN DEFAULT false
);
```

---

## Location Flow Recommendation

### Architecture: Hybrid Approach

1. **On User Login**: Load default location from user profile (DB)
2. **During Session**: Store current location in React state/context
3. **On Location Change**: 
   - Update React state immediately
   - Optionally save to DB if user wants to set as default
4. **On Each Search**: Send current lat/long in API request
   - Backend converts to geohash
   - Elasticsearch filters by geohash

### Why Send Location Each Time?
- ✅ User might be traveling (not at default location)
- ✅ Frontend has flexibility to change location without DB update
- ✅ Backend controls geohash precision
- ✅ Stateless API design

---

## Next Steps (Priority Order)

### Phase 1: Core Integration (This Week)
1. ✅ Backend APIs ready
2. 🔲 Setup PostgreSQL database
3. 🔲 Create User entity and authentication (Spring Security + JWT)
4. 🔲 Update frontend to call backend APIs
5. 🔲 Test location-based search

### Phase 2: Authentication (Next Week)
6. 🔲 Login/Register backend endpoints
7. 🔲 JWT token management in frontend
8. 🔲 Protected routes
9. 🔲 Cart persistence to DB

### Phase 3: UI Improvements
10. 🔲 Modern, engaging UI design
11. 🔲 Show delivery fees in comparison
12. 🔲 Improved combo display (even when all available)
13. 🔲 Save favorite products

### Phase 4: Advanced Features
14. 🔲 Price history tracking
15. 🔲 Price drop alerts
16. 🔲 Multiple saved locations per user

---

## Questions for You

1. **Database**: Confirm PostgreSQL or prefer another DB?
2. **Authentication**: Use Spring Security + JWT tokens?
3. **UI Framework**: Want to add TailwindCSS or keep Material-UI?
4. **Priority**: Start with authentication or API integration first?

---

## Testing the Backend

### Start Backend
```bash
cd /Users/Akash.Verma/IntellijProjects/moneyAndTimeSaver
./mvnw spring-boot:run -pl web
```

### Test API
```bash
# Search by location
curl "http://localhost:8080/api/products/search/by-location?query=paneer&latitude=28.5687&longitude=77.1886"

# Calculate cart
curl -X POST http://localhost:8080/api/cart/calculate \
  -H "Content-Type: application/json" \
  -d '{"items": [...]}'
```

---

**Ready to proceed? Let me know which phase you want to tackle first!**
