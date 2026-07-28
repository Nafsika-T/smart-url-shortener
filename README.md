# 🔗 Smart URL Shortener Platform

A URL shortening platform built with Spring Boot microservices, designed to demonstrate real-world backend concepts: Redis caching, Kafka event-driven analytics, JWT authentication, and clean service separation.

Users register, log in, and create short URLs. Every click is tracked asynchronously and enriched with geolocation and device data, giving URL owners real statistics without slowing down redirects. A dedicated API gateway sits in front of all three backend services, validating JWTs and routing requests so clients only ever need to talk to one address.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Spring Boot 3.5.x | Core framework |
| Spring Cloud Gateway (WebFlux) | Single entry point, JWT validation, request routing |
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
| [`api-gateway`](./api-gateway) | 8080 | ✅ Complete | Single entry point, JWT validation, request routing |
| [`auth-service`](./auth-service) | 8081 | ✅ Complete | User registration, login, JWT authentication |
| [`shortener-service`](./shortener-service) | 8082 | ✅ Complete | URL creation, Redis caching, Kafka event publishing |
| [`analytics-service`](./analytics-service) | 8083 | ✅ Complete | Kafka consumer — click enrichment & statistics |

**Also planned:** full Docker Compose (all services + databases), Kubernetes deployment, CI/CD pipeline.

## 🏗️ Architecture

```
Client
   │
   ▼
api-gateway:8080  ── validates JWT, forwards X-User-Id / X-User-Email
   │
   ├──▶ auth-service:8081                    (public)
   │         └── PostgreSQL (auth_db)
   │
   ├──▶ shortener-service:8082               (protected, except redirect)
   │         ├── PostgreSQL (shortener_db)
   │         ├── Redis (cache-aside, stores owner + active flag)
   │         └── Kafka producer ── publishes ClickEvent ──┐
   │                                                        │
   └──▶ analytics-service:8083                              │  (protected)
             ├── Kafka consumer ◀────────────────────────────┘
             ├── GeoLite2 (IP → country)
             ├── UserAgentUtils (User-Agent → device/browser)
             └── PostgreSQL (analytics_db)
```

`shortener-service` and `analytics-service` are fully decoupled — connected only through Kafka. A redirect never waits on analytics processing, and `analytics-service` can be scaled or restarted independently.

`api-gateway` is the only service clients should call directly. It validates the JWT on protected routes and attaches `X-User-Email`/`X-User-Id` headers before forwarding — downstream services no longer need callers to set those headers by hand. `analytics-service` additionally verifies that the caller actually owns the requested `shortCode`, independent of the gateway.

## 📁 Repository Structure

This is a single repository containing all services as independent Spring Boot projects:

```
smart-url-shortener/
├── api-gateway/
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

Start `auth-service`, `shortener-service`, and `analytics-service` first, then `api-gateway` — the gateway itself will start regardless of order, but requests through it won't succeed until the backend it's routing to is actually up.

| Service | URL |
|---|---|
| api-gateway | http://localhost:8080 |
| auth-service | http://localhost:8081 |
| shortener-service | http://localhost:8082 |
| analytics-service | http://localhost:8083 |

> `api-gateway` and `auth-service` must share the same `jwt.secret` — both default to the same fallback value if the `JWT_SECRET` environment variable isn't set, but if you override one in a real environment, override the other identically or token validation will fail.

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

All requests below go through `api-gateway` on port `8080`. The gateway validates the `Authorization` header and attaches `X-User-Id`/`X-User-Email` to protected requests automatically — callers no longer set those headers themselves. (Each service's own port is still reachable directly for local debugging, but `shortener-service` has no authentication of its own, so direct access should only be used for testing.)

### auth-service — public, no token required

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

### shortener-service — protected except redirect

**Create Short URL**
```
POST /api/urls
Authorization: Bearer {token}
```
```json
{ "originalUrl": "https://www.example.com/very/long/url" }
```

**Redirect** — public, no auth required
```
GET /{shortCode}
```
302 redirect to the original URL.

**Get All User URLs**
```
GET /api/urls
Authorization: Bearer {token}
```

**Deactivate URL**
```
PATCH /api/urls/{id}/deactivate
Authorization: Bearer {token}
```

**Delete URL**
```
DELETE /api/urls/{id}
Authorization: Bearer {token}
```

---

### analytics-service — protected, ownership-checked

**Total clicks**
```
GET /api/analytics/{shortCode}/total
Authorization: Bearer {token}
```

**Clicks by country**
```
GET /api/analytics/{shortCode}/by-country
Authorization: Bearer {token}
```
```json
[ { "country": "Greece", "total": 8 }, { "country": "Germany", "total": 3 } ]
```

**Clicks by device/browser**
```
GET /api/analytics/{shortCode}/by-device
Authorization: Bearer {token}
```
```json
[ { "deviceType": "Computer", "browser": "Chrome 15", "total": 5 } ]
```

**Click history**
```
GET /api/analytics/{shortCode}/history
Authorization: Bearer {token}
```

A caller only sees stats for shortCodes they own. If a shortCode has no click data yet, any authenticated caller can see the (empty/zero) result, since there's nothing yet to check ownership against.

## 🔧 Known Limitations & Planned Improvements

Tracked intentionally, rather than fixed reactively — these reflect deliberate sequencing decisions during development, not oversights.

| # | Item | Status |
|---|---|---|
| 1 | Add `timestamp` to `ErrorResponse` in `auth-service` (already present in the other services) | ⬜ Open |
| 2 | Align Spring Boot patch version across all services | ⬜ Open |
| 3 | Restrict Kafka `spring.json.trusted.packages` from `*` to explicit package names (production hardening) | ⬜ Open |
| 4 | Replace raw entity response in `/history` with a proper response DTO | ⬜ Open |
| 5 | Fix root `.gitignore` encoding — `.idea/` is meant to be excluded but currently isn't, due to the file being saved in the wrong text encoding | ⬜ Open |
| 6 | `api-gateway`'s error response `timestamp` serializes as a number array instead of an ISO date string, inconsistent with the other services | ⬜ Open |

## 🗺️ Roadmap

- [x] `auth-service`
- [x] `shortener-service`
- [x] `analytics-service`
- [x] `api-gateway`
- [ ] Full Docker Compose (all services + databases)
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline

## 📚 Documentation

Full implementation notes — architecture decisions, concept explanations, and complete request/response flows for every service:
pending
