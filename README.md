# 🔗 Smart URL Shortener Platform

A production-grade URL shortening platform built with **Spring Boot microservices**, similar to bit.ly.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Spring Boot 3.5.14 | Core framework |
| Spring Security + JWT | Authentication & authorization |
| Spring Data JPA + Hibernate | Database access |
| PostgreSQL | Primary database |
| Redis | Caching for fast URL redirects |
| Apache Kafka | Async event streaming for analytics |
| Spring Cloud Gateway | API Gateway & routing |
| Docker + Docker Compose | Containerization |
| Java 21 | Programming language |

## 📦 Services

| Service | Status | Description |
|---|---|---|
| `auth-service` | ✅ Complete | User registration, login, JWT authentication |
| `shortener-service` | 🔄 In Progress | URL creation, storage, and redirects |
| `analytics-service` | ⬜ Coming Soon | Click tracking and statistics via Kafka |
| `api-gateway` | ⬜ Coming Soon | Single entry point for all requests |

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

## 📡 API Endpoints

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
