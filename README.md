# java-http-app

Spring Boot application with PostgreSQL, Nginx, Prometheus and Grafana.

## Stack

| Component   | Version        |
|-------------|----------------|
| Java        | 21 (Temurin)   |
| Spring Boot | 4.0.5          |
| PostgreSQL  | 16             |
| Nginx       | 1.27-alpine    |
| Prometheus  | 2.52.0         |
| Grafana     | 11.0.0         |

## Project structure

```
java-http-app/
├── src/
│   └── main/
│       ├── java/org/post_hub/javahttpapp/
│       │   ├── JavaHttpAppApplication.java   # entry point
│       │   ├── HelloController.java          # GET /, GET /api/hello
│       │   └── DatabaseController.java       # GET /api/db-check, GET /api/users
│       └── resources/
│           ├── application.yaml              # datasource + actuator config
│           ├── schema.sql                    # DDL (runs on startup)
│           └── data.sql                      # seed data (runs on startup)
├── nginx/
│   └── java-http-app.conf                    # Nginx reverse proxy config
├── prometheus/
│   └── prometheus.yml                        # Prometheus scrape config
├── grafana/
│   └── provisioning/
│       ├── datasources/prometheus.yml        # auto-provision Prometheus datasource
│       └── dashboards/
│           ├── dashboards.yml                # dashboard provider config
│           └── java-http-app.json            # pre-built Spring Boot dashboard
├── Dockerfile                                # multi-stage image build
└── docker-compose.yml                        # full stack
```

## Prerequisites

- Docker Engine ≥ 24  
- Docker Compose v2 (included with Docker Desktop)

## Build the application image

```bash
docker build -t java-http-app:latest .
```

## Running the full stack

### 1. (Optional) copy and edit environment overrides

```bash
cp .env.example .env   # if .env.example exists, otherwise skip
```

Default credentials used when no `.env` is present:

| Variable         | Default        |
|------------------|----------------|
| DB_NAME          | appdb          |
| DB_USERNAME      | appuser        |
| DB_PASSWORD      | StrongPass123  |
| GF_ADMIN_USER    | admin          |
| GF_ADMIN_PASSWORD| admin          |

### 2. Start all services

```bash
docker compose up -d
```

### 3. Verify startup

```bash
docker compose ps
docker compose logs -f app
```

## Endpoints

| URL                              | Description                        |
|----------------------------------|------------------------------------|
| `http://localhost/`              | Hello via Nginx (port 80)          |
| `http://localhost/api/hello`     | Hello endpoint via Nginx           |
| `http://localhost/api/db-check`  | PostgreSQL connectivity check      |
| `http://localhost/api/users`     | List rows from test_users table    |
| `http://localhost:8080/actuator/prometheus` | Prometheus metrics (direct) |
| `http://localhost:9090`          | Prometheus UI                      |
| `http://localhost:3000`          | Grafana (admin / admin)            |

## Nginx reverse proxy

Nginx listens on **port 80** and proxies all requests to the Spring Boot application on port 8080.  
Config: `nginx/java-http-app.conf`

Verify the config inside the container:
```bash
docker exec nginx nginx -t
```

Difference from direct access: Nginx decouples the external port from the application port, adds proxy headers (`X-Real-IP`, `X-Forwarded-For`, etc.), enables SSL termination, and can serve as a single entry point for multiple upstream services.

## Prometheus

Config: `prometheus/prometheus.yml`  
Scrapes `/actuator/prometheus` from the `app` service every 15 s.

Open `http://localhost:9090/targets` — the `java-http-app` target should show **UP**.

Sample queries:
```promql
# HTTP requests per second
rate(http_server_requests_seconds_count{application="java-http-app"}[1m])

# p99 response time
histogram_quantile(0.99, rate(http_server_requests_seconds_bucket{application="java-http-app"}[1m]))

# JVM heap used
jvm_memory_used_bytes{area="heap", application="java-http-app"}
```

## Grafana

Open `http://localhost:3000` (admin / admin).

Prometheus is **auto-provisioned** as the default datasource.  
The **Java HTTP App** dashboard is auto-imported and shows:

- HTTP Requests per Second  
- HTTP Response Time (p50 / p99)  
- JVM Memory Used  
- Application Uptime  

## Stopping the stack

```bash
docker compose down
```

To also remove persistent volumes:
```bash
docker compose down -v
```

## Reproducing the environment from scratch

```bash
# 1. Install Docker (Ubuntu example)
sudo apt-get update && sudo apt-get install -y docker.io docker-compose-v2

# 2. Clone the repository
git clone <repo-url> && cd java-http-app

# 3. Build the image
docker build -t java-http-app:latest .

# 4. Start the stack
docker compose up -d

# 5. Smoke test
curl http://localhost/api/hello
curl http://localhost/api/db-check
```

## Manual build and run (without Docker)

```bash
# Build fat JAR
./mvnw clean package -DskipTests

# Run (requires a local PostgreSQL instance)
java -jar target/java-http-app-0.0.1-SNAPSHOT.jar \
  --DB_HOST=localhost \
  --DB_NAME=appdb \
  --DB_USERNAME=appuser \
  --DB_PASSWORD=secret
```
