# Authentication API Documentation

## Base URL
`http://localhost:8080`

---

## Public Endpoints (No Authentication Required)

### 1. Register User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "name": "John Doe",
  "userId": 1
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Email already registered"
}
```

---

### 2. Login User
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "name": "John Doe",
  "userId": 1
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid email or password"
}
```

---

## Protected Endpoints (Authentication Required)

**All protected endpoints require JWT token in the Authorization header:**
```
Authorization: Bearer <your-jwt-token>
```

### 3. Get Current User
**GET** `/api/user/me`

**Response (200 OK):**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "defaultLatitude": 28.5687,
  "defaultLongitude": 77.1886,
  "defaultGeohash": "ttnt7u5p9"
}
```

---

### 4. Test Authentication
**GET** `/api/user/test`

**Response (200 OK):**
```
Authentication working! User: user@example.com
```

---

## Testing with cURL

### Register:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123",
    "name": "Test User"
  }'
```

### Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123"
  }'
```

### Get Current User (replace TOKEN with actual token):
```bash
curl http://localhost:8080/api/user/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request (validation error, email exists) |
| 401 | Unauthorized (invalid credentials or token) |
| 403 | Forbidden (valid token but no permission) |
| 500 | Internal Server Error |
