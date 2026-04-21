# Nexora Deployment Guide

This guide is aligned to the current repository setup and deployment artifacts.

## 1) Deployment targets

- Local containerized stack via `compose.yaml`
- Kubernetes manifests in `k8s/`
- Render backend deployment via `render.yaml`

## 2) Prerequisites

- Java 17+
- Docker Desktop (or Docker Engine + Compose v2)
- Optional: Kubernetes cluster + `kubectl`

## 3) Environment setup

1. Copy `.env` template:

```bash
cp .env.example .env
```

2. Set secure values in `.env` at minimum:

- `JWT_SECRET`
- `POSTGRES_PASSWORD`
- `DB_PASSWORD`
- `GRAFANA_ADMIN_PASSWORD`

## 4) Local deployment with Docker Compose

Start full stack:

```bash
docker compose -f compose.yaml up -d --build
```

Check status:

```bash
docker compose -f compose.yaml ps
```

Stop stack:

```bash
docker compose -f compose.yaml down
```

## 5) Service endpoints (local)

- API: `http://localhost:8080`
- API via gateway: `http://localhost/api` (through nginx)
- Health: `http://localhost:8080/actuator/health`
- Frontend: `http://localhost:3000`
- Kafka UI: `http://localhost:8085`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001`

## 6) Kubernetes deployment

### 6.1 Prepare namespace and secrets

```bash
kubectl apply -f k8s/ingress.yaml
```

Create real secrets from template (do not commit real values):

```bash
kubectl apply -f k8s/secrets.example.yaml
```

### 6.2 Deploy workloads

```bash
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
```

### 6.3 Verify

```bash
kubectl get pods -n nexora
kubectl get svc -n nexora
kubectl get ingress -n nexora
```

Backend probes use:

- `/actuator/health/liveness`
- `/actuator/health/readiness`

## 7) Render deployment (backend)

`render.yaml` is configured for Docker runtime.

Required Render environment variables:

- `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`

Health check path:

- `/actuator/health`

## 8) Production hardening checklist

- Use managed PostgreSQL, Redis, and Kafka
- Rotate secrets and use secret manager
- Set `SPRING_PROFILES_ACTIVE=prod`
- Restrict ingress to trusted domains
- Enable TLS certificates
- Set backup and restore policy for PostgreSQL
- Configure alerting for health, latency, error rate
- Add autoscaling policies for backend pods

## 9) Validation commands

```bash
# Validate Spring project compiles
./gradlew compileJava

# Validate frontend build
cd frontend && npm ci && npm run build

# Validate compose file
docker compose -f compose.yaml config
```

## 10) Related files

- `compose.yaml`
- `Dockerfile`
- `nginx.conf`
- `src/main/resources/application.yml`
- `k8s/backend-deployment.yaml`
- `k8s/frontend-deployment.yaml`
- `k8s/ingress.yaml`
- `k8s/secrets.example.yaml`
- `render.yaml`
