# 🔗 Smart URL Shortener Platform

A URL shortening platform built with Spring Boot microservices, designed to demonstrate real-world concepts like Redis caching, Kafka messaging, and JWT authentication.

Users can register, log in, and create short URLs. Every click is tracked with analytics, and each user has a credits system managed with database transactions.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Spring Boot 3.5.15 | Core framework |
| Spring Security + JWT | Authentication & authorization |
| Spring Data JPA + Hibernate | Database access |
| PostgreSQL | Primary database |
| Redis | Caching for fast URL redirects |
| Apache Kafka | Async event streaming for analytics |
| Spring Cloud Gateway | API Gateway & routing |
| Docker + Docker Compose | Containerization |
| Java 21 | Programming language |

## 📦 Services

| Service | Port | Status | Description |
|---|---|---|---|
| `auth-service` | 8081 | ✅ Complete | User registration, login, JWT authentication |
| `shortener-service` | 8082 | ✅ Complete | URL creation, Redis caching, Kafka events |
| `analytics-service` | 8083 | ⬜ Coming Soon | Click tracking and statistics via Kafka |
| `api-gateway` | 8080 | ⬜ Coming Soon | Single entry point for all requests |

## 🏗️ Architecture

```
Client
   │
   ▼
api-gateway:8080        (coming soon)
   │
   ├──▶ auth-service:8081
   │         └── PostgreSQL (auth_db)
   │
   ├──▶ shortener-service:8082
   │         ├── PostgreSQL (shortener_db)
   │         ├── Redis (cache)
   │         └── Kafka (publishes click events)
   │
   └──▶ analytics-service:8083  (coming soon)
             ├── PostgreSQL (analytics_db)
             └── Kafka (consumes click events)
```

## 🚀 Running auth-service

**1. Create the database:**
```bash
psql -U postgres
CREATE DATABASE auth_db;
```

**2. Run the service:**
```bash
./mvnw spring-boot:run
```

Service runs on `http://localhost:8081`

## 📡 auth-service API Endpoints

### Register
```
POST /api/auth/register
```
```json
{
    "username": "john",
    "email": "john@email.com",
    "password": "password123"
}
```

### Login
```
POST /api/auth/login
```
```json
{
    "email": "john@email.com",
    "password": "password123"
}
```

### Response
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "john"
}
```

## 🔐 Security
- JWT authentication — tokens expire after 24 hours
- BCrypt password hashing
- Stateless — no sessions

## 🚀 Running shortener-service

**1. Create the database:**
```bash
psql -U postgres
CREATE DATABASE shortener_db;
```

**2. Start Redis and Kafka with Docker:**
```bash
docker-compose up -d
```

**3. Run the service:**
```bash
./mvnw spring-boot:run
```

Service runs on `http://localhost:8082`

## 📡 shortener-service API Endpoints

### Create Short URL
```
POST /api/urls
X-User-Id: {userId}
```
```json
{
    "originalUrl": "https://www.example.com/very/long/url"
}
```

### Redirect
```
GET /{shortCode}
```
Redirects to the original URL (302 Found)

### Get All User URLs
```
GET /api/urls
X-User-Id: {userId}
```

### Deactivate URL
```
PATCH /api/urls/{id}/deactivate
X-User-Id: {userId}
```

### Delete URL
```
DELETE /api/urls/{id}
X-User-Id: {userId}
```

## ⚡ Key Features
- **Redis cache-aside pattern** — short codes cached for 24h for instant redirects
- **Kafka async messaging** — click events published without slowing down redirects
- **Ownership validation** — users can only modify their own URLs
- **`@Transactional`** — atomic operations for data consistency

## 📚 Documentation

Full implementation notes including architecture decisions,
concept explanations and complete flows:
[View Documentation](https://docs.google.com/document/d/1jS0ol4TuV5lZnwfOzOngq3a2R8FxzJ3m/edit?usp=sharing&ouid=110393881445813149836&rtpof=true&sd=true)

[View Documentation](https://docs.google.com/document/d/1jS0ol4TuV5lZnwfOzOngq3a2R8FxzJ3m/edit?usp=sharing&ouid=110393881445813149836&rtpof=true&sd=true
)

