# 🔗 Smart URL Shortener Platform

A URL shortening platform built with Spring Boot microservices, designed to demonstrate real-world backend concepts: Redis caching, Kafka event-driven analytics, JWT authentication, and clean service separation.

Users register, log in, and create short URLs. Every click is tracked asynchronously and enriched with geolocation and device data, giving URL owners real statistics without slowing down redirects. A dedicated API gateway sits in front of all three backend services, validating JWTs and routing requests so clients only ever need to talk to one address. A lightweight vanilla HTML/CSS/JavaScript frontend calls the API directly, covering registration, login, URL management, and the analytics dashboard.

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
| HTML/CSS/JavaScript (vanilla) | Frontend — no framework or build step |

## 📦 Services

| Service | Port | Status | Description |
|---|---|---|---|
| [`api-gateway`](./api-gateway) | 8080 | ✅ Complete | Single entry point, JWT validation, request routing |
| [`auth-service`](./auth-service) | 8081 | ✅ Complete | User registration, login, JWT authentication |
| [`shortener-service`](./shortener-service) | 8082 | ✅ Complete | URL creation, Redis caching, Kafka event publishing |
| [`analytics-service`](./analytics-service) | 8083 | ✅ Complete | Kafka consumer — click enrichment & statistics |

**Also planned:** Kubernetes deployment, CI/CD pipeline.

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
├── api-gateway/          (includes Dockerfile)
├── auth-service/         (includes Dockerfile)
├── shortener-service/    (includes Dockerfile)
├── analytics-service/    (includes Dockerfile)
├── frontend/             (vanilla HTML/CSS/JS — login.html, dashboard.html)
├── postgres-init/        (DB creation script for Docker Compose)
├── docker-compose.yml    (full stack: infra + all 4 services)
└── README.md
```

Each service has its own `pom.xml`, is independently runnable, and owns its own database — no shared code or shared data between services.

## 🚀 Getting Started

### ⚠️ Required first, for either option below: `analytics-service`'s GeoLite2 file

The GeoLite2 database file (`GeoLite2-City.mmdb`) is **not included in this repository**, per MaxMind's license terms. Without it, `analytics-service` fails to start — whether run locally or built into a container, since it's read from the compiled classpath either way.

1. Create a free account at [maxmind.com/en/geolite2/signup](https://www.maxmind.com/en/geolite2/signup)
2. Generate a license key and download **GeoLite2-City** (binary `.mmdb` format)
3. Place the file at:
   ```
   analytics-service/src/main/resources/geoip/GeoLite2-City.mmdb
   ```

### Option A — Docker Compose (recommended)

Runs the entire stack — Postgres, Redis, Kafka/Zookeeper, and all four services — with one command. Requires only Docker Desktop.

```bash
docker-compose up --build
```

This builds each service's image, creates the three databases automatically (via `postgres-init/init-databases.sql`), and starts everything on a shared Docker network.

| Service | URL |
|---|---|
| api-gateway | http://localhost:8080 |
| auth-service | http://localhost:8081 |
| shortener-service | not exposed to host — reachable only via the gateway |
| analytics-service | not exposed to host — reachable only via the gateway |

Secrets (`DB_PASSWORD`, `JWT_SECRET`) load from a git-ignored `.env` file at the project root. To stop everything: `docker-compose down` (add `-v` to also wipe the Postgres volume).

**Using the app:** open `frontend/login.html` in your browser once containers are up.

> Port 5432 already in use? A native Postgres install is likely holding it — stop that service, or ignore it, since the containers talk to Postgres over Docker's internal network either way.

### Option B — Run locally (Java/Maven, no containers for the apps)

Useful for active development, debugging, or IDE breakpoints.

**Prerequisites:** Java 21, Maven, PostgreSQL (running locally), Docker Desktop (for Redis & Kafka only).

**1. Start shared infrastructure (Redis, Zookeeper, Kafka)**
```bash
cd shortener-service
docker-compose up -d
```

**2. Create the databases**
```bash
psql -U postgres
CREATE DATABASE auth_db;
CREATE DATABASE shortener_db;
CREATE DATABASE analytics_db;
```

**3. Run each service**
From each service's folder:
```bash
./mvnw spring-boot:run
```

Start `auth-service`, `shortener-service`, and `analytics-service` first, then `api-gateway`. Unlike Docker Compose, this exposes every service directly on `localhost` (8080–8083), for easier debugging.

> `api-gateway` and `auth-service` must use the same `jwt.secret` (both default to `changeme` if `JWT_SECRET` isn't set) or token validation will fail.

## 📡 API Reference

All requests below go through `api-gateway` on port `8080`. The gateway validates the `Authorization` header and attaches `X-User-Id`/`X-User-Email` to protected requests automatically — callers no longer set those headers themselves. In Docker Compose, `shortener-service` and `analytics-service` have no authentication of their own and are not reachable directly (see Getting Started above) — `api-gateway` is the only supported entry point. (Running via Option B/local Maven exposes every service's own port directly, for debugging only.)

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


| # | Item                                                                                                                                                                                                        | Status |
|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| 1 | Add ownership (`userId`) checks to `analytics-service`'s stats endpoints                                                                                                                                    | ✅ Fixed |
| 2 | Embed `userId` as a claim in the JWT at login/register time                                                                                                                                                 | ✅ Fixed |
| 3 | Remove synchronous `clickCount` updates from `shortener-service` — click counting is now handled entirely and asynchronously by `analytics-service` via Kafka, keeping Redis cache hits fully database-free | ✅ Fixed |
| 4 | Add `timestamp` to `ErrorResponse` in `auth-service` (already present in the other services)                                                                                                                | ✅ Fixed |
| 5 | Align Spring Boot patch version across all services                                                                                                                                                         | ✅ Fixed |
| 6 | Restrict Kafka `spring.json.trusted.packages` from `*` to explicit package names (production hardening)                                                                                                     | ✅ Fixed |
| 7 | Replace raw entity response in `/history` with a proper response DTO                                                                                                                                        | ✅ Fixed |
| 8 | Fix root `.gitignore` encoding — `.idea/` is meant to be excluded but currently isn't, due to the file being saved in the wrong text encoding                                                               | ✅ Fixed |
| 9 | `api-gateway`'s error response `timestamp` serializes as a number array instead of an ISO date string, inconsistent with the other services                                                                 | ✅ Fixed |
| 10 | `shortener-service`/`analytics-service` ports were published directly to the host, letting anyone bypass `api-gateway`'s JWT check entirely                                                                 | ✅ Fixed |
| 11 | DB password and JWT secret were hardcoded and committed to source control                                                                                                                                   | ✅ Fixed |
| 12 | Generated short URLs pointed at `shortener-service`'s now-unreachable direct port instead of `api-gateway`                                                                                                  | ✅ Fixed |
| 13 | No format/scheme validation on submitted URLs — `originalUrl` is only checked for non-blank + length                                                                                                        | ⬜ Open |
| 14 | No way to reactivate a deactivated URL — `deactivate` endpoint exists, no `activate` endpoint                                                                                                               | ⬜ Open |

## 🗺️ Roadmap

- [x] `auth-service`
- [x] `shortener-service`
- [x] `analytics-service`
- [x] `api-gateway`
- [x] Full Docker Compose (all services + databases)
- [x] Frontend
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline

## 📚 Documentation

Full implementation notes — architecture decisions, concept explanations, and complete request/response flows for every service:
pending
