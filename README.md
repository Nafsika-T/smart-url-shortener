# 🔗 Smart URL Shortener Platform

A URL shortening platform built with Spring Boot microservices, designed to demonstrate real-world backend concepts: Redis caching, Kafka event-driven analytics, JWT authentication, and clean service separation.

Users register, log in, and create short URLs. Every click is tracked asynchronously and enriched with geolocation and device data, giving URL owners real statistics without slowing down redirects.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Spring Boot 3.5.x | Core framework |
| Spring Security + JWT | Authentication & authorization |
| Spring Data JPA + Hibernate | Database access |
| PostgreSQL | Primary database (one per service) |
| Redis | Caching for fast URL redirects |
| Apache Kafka | Async event streaming for analytics |
| MaxMind GeoLite2 | IP geolocation for click analytics |
| UserAgentUtils | Browser/device detection for click analytics |
| Docker + Docker Compose | Containerization |
| Java 21 | Programming language |

## 📦 Services

| Service | Port | Status | Description |
|---|---|---|---|
| [`auth-service`](./auth-service) | 8081 | ✅ Complete | User registration, login, JWT authentication |
| [`shortener-service`](./shortener-service) | 8082 | ✅ Complete | URL creation, Redis caching, Kafka event publishing |
| [`analytics-service`](./analytics-service) | 8083 | ✅ Complete | Kafka consumer — click enrichment & statistics |
| `api-gateway` | 8080 | ⬜ Planned | Single entry point, JWT validation, request routing |

**Also planned:** full Docker Compose (all services + databases), Kubernetes deployment, CI/CD pipeline.

## 🏗️ Architecture

```
Client
   │
   ▼
api-gateway:8080                    (planned)
   │
   ├──▶ auth-service:8081
   │         └── PostgreSQL (auth_db)
   │
   ├──▶ shortener-service:8082
   │         ├── PostgreSQL (shortener_db)
   │         ├── Redis (cache-aside, stores owner + active flag)
   │         └── Kafka producer ── publishes ClickEvent ──┐
   │                                                        │
   └──▶ analytics-service:8083                              │
             ├── Kafka consumer ◀────────────────────────────┘
             ├── GeoLite2 (IP → country)
             ├── UserAgentUtils (User-Agent → device/browser)
             └── PostgreSQL (analytics_db)
```

`shortener-service` and `analytics-service` are fully decoupled — connected only through Kafka. A redirect never waits on analytics processing, and `analytics-service` can be scaled or restarted independently.

## 📁 Repository Structure

This is a single repository containing all services as independent Spring Boot projects:

```
smart-url-shortener/
├── auth-service/
├── shortener-service/
├── analytics-service/
└── README.md
```

Each service has its own `pom.xml`, is independently runnable, and owns its own database — no shared code or shared data between services.

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven
- PostgreSQL (running locally)
- Docker Desktop (for Redis & Kafka)

### 1. Start shared infrastructure (Redis, Zookeeper, Kafka)
```bash
cd shortener-service
docker-compose up -d
```

### 2. Create the databases
```bash
psql -U postgres
CREATE DATABASE auth_db;
CREATE DATABASE shortener_db;
CREATE DATABASE analytics_db;
```

### 3. Run each service
From each service's folder:
```bash
./mvnw spring-boot:run
```

| Service | URL |
|---|---|
| auth-service | http://localhost:8081 |
| shortener-service | http://localhost:8082 |
| analytics-service | http://localhost:8083 |

### ⚠️ Extra setup required for `analytics-service`

The GeoLite2 database file (`GeoLite2-City.mmdb`) is **not included in this repository**, per MaxMind's license terms. To run `analytics-service` locally:

1. Create a free account at [maxmind.com/en/geolite2/signup](https://www.maxmind.com/en/geolite2/signup)
2. Generate a license key and download **GeoLite2-City** (binary `.mmdb` format)
3. Place the file at:
   ```
   analytics-service/src/main/resources/geoip/GeoLite2-City.mmdb
   ```

Without this file, `analytics-service` will fail to start.

## 📡 API Reference

### auth-service

**Register**
```
POST /api/auth/register
```
```json
{ "username": "john", "email": "john@email.com", "password": "password123" }
```

**Login**
```
POST /api/auth/login
```
```json
{ "email": "john@email.com", "password": "password123" }
```
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "username": "john" }
```

**Security:** JWT authentication (24h expiry), BCrypt password hashing, stateless — no sessions.

---

### shortener-service

**Create Short URL**
```
POST /api/urls
X-User-Id: {userId}
```
```json
{ "originalUrl": "https://www.example.com/very/long/url" }
```

**Redirect**
```
GET /{shortCode}
```
302 redirect to the original URL. Public endpoint, no auth required.

**Get All User URLs**
```
GET /api/urls
X-User-Id: {userId}
```

**Deactivate URL**
```
PATCH /api/urls/{id}/deactivate
X-User-Id: {userId}
```

**Delete URL**
```
DELETE /api/urls/{id}
X-User-Id: {userId}
```

---

### analytics-service

**Total clicks**
```
GET /api/analytics/{shortCode}/total
```

**Clicks by country**
```
GET /api/analytics/{shortCode}/by-country
```
```json
[ { "country": "Greece", "total": 8 }, { "country": "Germany", "total": 3 } ]
```

**Clicks by device/browser**
```
GET /api/analytics/{shortCode}/by-device
```
```json
[ { "deviceType": "Computer", "browser": "Chrome 15", "total": 5 } ]
```

**Click history**
```
GET /api/analytics/{shortCode}/history
```

## 🔧 Known Limitations & Planned Improvements

Tracked intentionally, rather than fixed reactively — these reflect deliberate sequencing decisions during development, not oversights.

| # | Item | Status |
|---|---|---|
| 1 | Add `timestamp` to `ErrorResponse` in `auth-service` (already present in the other two services) | ⬜ Open |
| 2 | Align Spring Boot patch version across all three services | ⬜ Open |
| 3 | Restrict Kafka `spring.json.trusted.packages` from `*` to explicit package names (production hardening) | ⬜ Open |
| 4 | Add ownership (`userId`) checks to `analytics-service`'s stats endpoints | ⬜ Open |
| 5 | Replace raw entity response in `/history` with a proper response DTO | ⬜ Open |
| 6 | Embed userId as a claim in the JWT at login/register time | ⬜ Open | 

**Resolved during development, kept here for context:**
- ~~Store active flag in Redis cache~~ — investigated, found unnecessary: `deactivateUrl()` already evicts the Redis entry on every deactivation, so a deactivated URL can never remain cached.
- Removed synchronous `clickCount` updates from `shortener-service` — click counting is now handled entirely and asynchronously by `analytics-service` via Kafka, keeping Redis cache hits fully database-free.

## 🗺️ Roadmap

- [x] `auth-service`
- [x] `shortener-service`
- [x] `analytics-service`
- [ ] `api-gateway`
- [ ] Full Docker Compose (all services + databases)
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline

## 📚 Documentation

Full implementation notes — architecture decisions, concept explanations, and complete request/response flows for every service:
pending
