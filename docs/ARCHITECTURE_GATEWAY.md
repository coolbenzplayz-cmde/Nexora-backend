# API gateway and service extraction

## Current state

All domain packages run inside **one Spring Boot process**. That is appropriate for early development and for teams that want a single deployment unit while APIs stabilize.

## API Gateway (recommended next step)

In the full Nexora topology, an **API Gateway** sits in front of services and is responsible for:

- TLS termination and **JWT validation** at the edge (optional duplication with service-level security during migration)
- **Routing** to `auth-service`, `user-service`, `game-service`, etc.
- **Rate limiting** and abuse protection
- **Request logging**, tracing headers, and correlation IDs
- **CORS** and mobile/web client policy in one place

Common implementations:

- **Spring Cloud Gateway** (Reactive) on Kubernetes or VM scale sets
- Managed gateways (AWS API Gateway, Azure APIM, Kong, Traefik plugins) with auth plugins

This repository does **not** include a separate gateway module yet; add a sibling project `nexora-gateway` when you split traffic, or a `gateway` submodule using `spring-cloud-starter-gateway`.

## Extracting a microservice

Practical order of extraction (low coupling first):

1. **game-service** — clear boundary: sessions, scores, leaderboards; events to wallet.
2. **media-service** — async jobs, object storage, workers; heavy CPU off the main API.
3. **messaging-service** — WebSocket clusters and presence often need their own scale unit.

For each extraction:

1. Move package to a new Gradle project with its own `Dockerfile` and DB schema (or schema per service).
2. Replace in-process calls with **HTTP** or **gRPC** clients; keep DTOs stable.
3. Subscribe consumers to the same **Kafka** topics (or namespace topics per environment).
4. Point the gateway route `/api/games/**` to the new host.

## Identity and security

Keep **one issuer** for JWTs (auth-service) and the same signing keys across services during migration, or move to **OAuth2 / OIDC** with a central IdP for long-term federation.
