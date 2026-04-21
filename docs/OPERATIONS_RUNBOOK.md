# Operations Runbook

## Services
- Backend API: Spring Boot (`src/main/java/org/example/nexora`)
- Frontend: Vite/React (`frontend/`)
- Reverse proxy: Nginx (`nginx.conf`)
- Container orchestration: Docker Compose (`compose.yaml`) / Kubernetes (`k8s/`)

## 1. Pre-Deploy Checklist
1. Confirm secrets are configured from `.env` (not committed).
2. Run tests:
   - `./gradlew test` (Windows: `gradlew.bat test`)
3. Validate config:
   - `src/main/resources/application.yml`
   - `k8s/*.yaml`
4. Verify DB migration/schema compatibility.

## 2. Deployment (Docker Compose)
1. Build images:
   - `docker compose build`
2. Start stack:
   - `docker compose up -d`
3. Verify health endpoints and logs.

## 3. Deployment (Kubernetes)
1. Create namespace and secrets.
2. Apply manifests:
   - `kubectl apply -f k8s/`
3. Verify pods, services, ingress:
   - `kubectl get pods,svc,ingress -n <namespace>`

## 4. Health Verification
- API health endpoint should return OK.
- Check authentication flow (login/token refresh).
- Check payment webhook endpoint reachability.
- Ensure websocket connections are authenticated.

## 5. Incident Response
### Unauthorized Access Suspected
1. Revoke affected sessions/tokens.
2. Rotate signing keys and critical secrets.
3. Inspect auth and API logs.
4. Force password reset for impacted accounts.

### Payment Fraud Spike
1. Enable strict fraud thresholds.
2. Temporarily throttle high-risk endpoints.
3. Queue suspicious transactions for manual review.

### Elevated 5xx Errors
1. Check container/pod health and restarts.
2. Review latest deploy diff and rollback if needed.
3. Inspect DB connection pool and queue backlog.

## 6. Backup & Restore
- Daily encrypted DB backups.
- Weekly restore drill in non-production.
- Keep recovery steps documented and tested.

## 7. On-Call Escalation
- Primary: Backend engineer on call
- Secondary: Platform/DevOps engineer
- Tertiary: Security lead for incidents involving auth/payments
