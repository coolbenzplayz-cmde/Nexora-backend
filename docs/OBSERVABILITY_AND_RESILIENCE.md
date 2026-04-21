# Observability & Resilience Guide

This document defines practical defaults for production-grade reliability in Nexora.

## 1) Logging
- Use structured logs for API requests, auth events, payments, and security warnings.
- Avoid logging secrets, tokens, OTPs, or card/payment sensitive values.
- Use correlation IDs (`X-Request-Id`) across services for traceability.

## 2) Metrics
Recommended baseline metrics:
- JVM CPU/heap/GC
- HTTP request count, latency (p50/p95/p99), error rate
- Authentication success/failure rates
- Payment success/failure/retry rates
- Queue lag and websocket connection counts

## 3) Alerting
Create alerts for:
- 5xx error rate > 5% for 5 minutes
- auth failures spike (possible brute force)
- payment failures spike
- pod restart loops / unhealthy probes
- high latency p95 regressions

## 4) Resilience Controls
- Timeouts on all outbound calls
- Retries with exponential backoff for transient failures
- Circuit breakers on critical external integrations
- Bulkheads for payment and messaging workloads

## 5) Kubernetes Reliability Baseline
- Horizontal Pod Autoscaler for backend
- PodDisruptionBudget to keep minimum healthy pods
- NetworkPolicy to reduce lateral movement risk
- Separate readiness and liveness probes

## 6) Recovery Targets
- Suggested RPO: 15 minutes
- Suggested RTO: 60 minutes
- Run restore drills at least monthly

## 7) Incident Artifacts
Maintain:
- Incident timeline
- Root cause summary
- Corrective actions (owner + due date)
- Follow-up hardening tasks
