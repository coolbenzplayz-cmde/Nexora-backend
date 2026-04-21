# Nexora Deployment Validation Checklist

Use this checklist before staging or production rollout.

## A. Configuration & Secrets

- [ ] `.env` created from `.env.example`
- [ ] `JWT_SECRET` set to strong random value
- [ ] DB credentials rotated from defaults
- [ ] Grafana admin credentials changed
- [ ] No secrets committed to git

## B. Backend readiness

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `/actuator/health` returns `UP`
- [ ] `/actuator/health/liveness` returns `UP`
- [ ] `/actuator/health/readiness` returns `UP`
- [ ] DB connection stable under load
- [ ] Redis connectivity validated
- [ ] Kafka connectivity validated

## C. Frontend readiness

- [ ] Frontend container serves on port 80
- [ ] Nginx reverse proxy routes `/api` to backend
- [ ] SPA route fallback works (`/index.html`)

## D. Compose readiness

- [ ] `docker compose -f compose.yaml config` passes
- [ ] `docker compose -f compose.yaml up -d --build` passes
- [ ] `docker compose -f compose.yaml ps` shows healthy services

## E. Kubernetes readiness

- [ ] Namespace exists (`nexora`)
- [ ] Secret exists (`nexora-secrets`)
- [ ] Backend deployment available
- [ ] Frontend deployment available
- [ ] Ingress routes API and frontend correctly
- [ ] TLS secret configured for real domain

## F. Operational readiness

- [ ] Logs are centralized
- [ ] Metrics dashboards available (Prometheus/Grafana)
- [ ] Error-rate and latency alerts configured
- [ ] Backup and restore tested
- [ ] Rollback procedure documented and tested
